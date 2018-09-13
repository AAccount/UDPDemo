package jniudp.dt.jniudp;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

public class MainActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener
{
	private Button button;
	private TextView textView;
	private EditText ip, port;
	private Switch code;
	private boolean running = false;
	private int seq, total;
	private String ip2;
	private int port2;
	private DatagramSocket javaudp;
	private InetAddress receiver;
	private boolean useNative;

	@Override
	protected void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		button = findViewById(R.id.main_button);
		button.setOnClickListener(this);
		textView = findViewById(R.id.main_textview);
		ip = findViewById(R.id.main_addr);
		port = findViewById(R.id.main_port);
		code = findViewById(R.id.main_native);
		code.setOnCheckedChangeListener(this);
	}

	@Override
	public void onClick(View v)
	{
		if(running)
		{
			button.setText("Stopping");
			running = false;
		}
		else
		{
			button.setText("Starting");
			seq = 0; total =0;
			running = true;
			networkThread();
		}
	}

	private void networkThread()
	{
		Thread thread = new Thread(new Runnable()
		{
			@Override
			public void run()
			{
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						button.setText("Thread Started, native? " + useNative);
						ip2 = ip.getText().toString();
						port2 = Integer.valueOf(port.getText().toString());
					}
				});

				if(useNative)
				{
					JNI.setupSocket(ip2, port2);
				}
				else
				{
					try
					{
						javaudp = new DatagramSocket();
						javaudp.setTrafficClass(0x2E << 2);
						receiver = InetAddress.getByName(ip2);
					}
					catch (SocketException e)
					{
						e.printStackTrace();
					}
					catch (UnknownHostException e)
					{
						e.printStackTrace();
					}
				}

				while(running)
				{
					String message = "blah blah blah filler sending #: " + seq + "\n";
					byte[] bytes = new byte[1000];
					System.arraycopy(message.getBytes(), 0, bytes, 0, message.length());

					if(useNative)
					{
						JNI.send(bytes);
					}
					else
					{
						DatagramPacket datagramPacket = new DatagramPacket(bytes, bytes.length, receiver, port2);
						try
						{
							javaudp.send(datagramPacket);
						}
						catch (IOException e)
						{
							e.printStackTrace();
						}
					}

					seq++;
					total = total + bytes.length;
					runOnUiThread(new Runnable()
					{
						@Override
						public void run()
						{
							textView.setText("Sent #: " + seq + " Total: " + total);
						}
					});
					try
					{
						Thread.sleep(50);
					}
					catch (InterruptedException e)
					{
						e.printStackTrace();
					}
				}

				if(useNative)
				{
					JNI.close();
				}
				else
				{
					javaudp.close();
				}
				runOnUiThread(new Runnable()
				{
					@Override
					public void run()
					{
						button.setText("Thread Stopped");
					}
				});
			}
		});
		thread.start();
	}

	@Override
	public void onCheckedChanged(CompoundButton buttonView, boolean isChecked)
	{
		useNative = isChecked;
		String text = isChecked ? "Native" : "Java";
		code.setText(text);
	}
}
