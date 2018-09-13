package jniudp.dt.jniudp;

public class JNI
{
	static
	{
		System.loadLibrary("jniudp");
	}

	public static native void setupSocket(String ip, int port);
	public static native boolean send(byte[] out);
	public static native boolean receive(byte[] in);
	public static native void close();
}
