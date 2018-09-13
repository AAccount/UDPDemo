#include <jni.h>
#include <sys/types.h>
#include <sys/socket.h>
#include <netinet/in.h>
#include <arpa/inet.h>
#include <string.h>
#include <stdbool.h>
#include <unistd.h>
#include <stdlib.h>
#include <errno.h>

int fd;
struct sockaddr_in info;

JNIEXPORT void JNICALL
Java_jniudp_dt_jniudp_JNI_setupSocket(JNIEnv *env, jclass type, jstring ip_, jint port)
{
    const char *ip = (*env)->GetStringUTFChars(env, ip_, 0);
    u_int32_t ipInt = 0;
    inet_pton(AF_INET, ip, &ipInt);

    fd = socket(AF_INET, SOCK_DGRAM, 0);
    if(fd < 0)
    {
        int placeholder = 5;
        placeholder++;
    }
    memset((char*) &info, 0, sizeof(struct sockaddr_in));
    info.sin_family = AF_INET; //ipv4
    info.sin_addr.s_addr = ipInt; //listen on any nic
    info.sin_port = htons(port);

    (*env)->ReleaseStringUTFChars(env, ip_, ip);
}

JNIEXPORT jboolean JNICALL
Java_jniudp_dt_jniudp_JNI_send(JNIEnv *env, jclass type, jbyteArray out_)
{
    jbyte *out = (*env)->GetByteArrayElements(env, out_, NULL);
    jsize length = (*env)->GetArrayLength(env, out_);
    ssize_t result = sendto(fd, out, (size_t )length, NULL, (struct sockaddr*)&info, sizeof(info));
    (*env)->ReleaseByteArrayElements(env, out_, out, 0);
    return result < 0;
}

JNIEXPORT jboolean JNICALL
Java_jniudp_dt_jniudp_JNI_receive(JNIEnv *env, jclass type, jbyteArray in_)
{
    jbyte* buffer = malloc(1500);
    ssize_t result = recv(fd, buffer, 1500, NULL);
    if(result < 0)
    {
        free(buffer);
        return false;
    }
    (*env)->SetByteArrayRegion(env, in_, 0, result, buffer);
    free(buffer);
    return true;
}

JNIEXPORT void JNICALL
Java_jniudp_dt_jniudp_JNI_close(JNIEnv *env, jclass type)
{
    close(fd);
    memset((char*) &info, 0, sizeof(struct sockaddr_in));
}