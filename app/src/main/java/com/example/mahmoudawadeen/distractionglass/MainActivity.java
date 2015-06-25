package com.example.mahmoudawadeen.distractionglass;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.widget.CardBuilder;
import com.google.android.glass.widget.CardScrollAdapter;
import com.google.android.glass.widget.CardScrollView;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;


public class MainActivity extends Activity {

    /**
     * {@link CardScrollView} to use as the main content view.
     */
    private CardScrollView mCardScroller;

    private final static int PORT = 4747;
    private final static String ADDRESS = "137.250.171.235";

    private static String message = "on";

    private View mView;
    private boolean startSignalRecieved;


    private boolean good;
    private boolean firstTime = true;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        mView = buildView();

        mCardScroller = new CardScrollView(this);
        mCardScroller.setAdapter(new CardScrollAdapter() {
            @Override
            public int getCount() {
                return 1;
            }

            @Override
            public Object getItem(int position) {
                return mView;
            }

            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                return mView;
            }

            @Override
            public int getPosition(Object item) {
                if (mView.equals(item)) {
                    return 0;
                }
                return AdapterView.INVALID_POSITION;
            }
        });
        // Handle the TAP event.
        mCardScroller.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                // Plays disallowed sound to indicate that TAP actions are not supported.
                AudioManager am = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                am.playSoundEffect(Sounds.DISALLOWED);
            }
        });
        setContentView(mCardScroller);
        AsyncTask receiver = new receiveStartSignal();
        receiver.execute("hey");
    }

    @Override
    protected void onResume() {
        super.onResume();
        mCardScroller.activate();
    }

    @Override
    protected void onPause() {
        mCardScroller.deactivate();
        super.onPause();
    }

    /**
     * Builds a Glass styled "Hello World!" view using the {@link CardBuilder} class.
     */
    private View buildView() {
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.ALERT);
        card.setIcon(R.drawable.sand_clock);
        card.setText("Waiting for start signal");
        card.setFootnote("start typing to automatically send the start signal");
        return card.getView();
    }

    class sendAction extends AsyncTask {

        @Override
        protected Object doInBackground(Object[] params) {
            try {
                DatagramSocket socket = new DatagramSocket();
                byte[] buf = message.getBytes();
                InetAddress hostAddress = InetAddress.getByName(((String) params[0]));
                DatagramPacket dgp = new DatagramPacket(buf, buf.length, hostAddress, (int) params[1]);
                socket.send(dgp);
                message = (message.equals("on") ? "off" : "on");
            } catch (IOException e) {
                e.printStackTrace();
            }
            return (message.equals("on") ? "off" : "on");
        }

        @Override
        protected void onPostExecute(Object result) {
//            CardBuilder card = new CardBuilder(MainActivity.this, CardBuilder.Layout.TEXT);
//            currentCapsLockImage = (result.equals("on") ? R.drawable.on : R.drawable.off);
            ImageView img = (ImageView) findViewById(R.id.imageView3);
            img.setImageResource((result.equals("on") ? R.drawable.on_square : R.drawable.off_square));
//            card.addImage((result.equals("on") ? R.drawable.on : R.drawable.off));
//            card.addImage((good) ? R.drawable.thumb_positive : R.drawable.thumb_negative);

        }
    }

    class receiveStartSignal extends AsyncTask<Object, Object, String> {

        @Override
        protected String doInBackground(Object[] params) {
            try {
                byte[] buf = new byte[1000];
                DatagramSocket socket = new DatagramSocket(34144);
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                socket.receive(datagramPacket);
                socket.close();
                return new String(datagramPacket.getData(), 0, datagramPacket.getLength());
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            if (result.equals("start")) {
                setContentView(R.layout.iconlayout);
                startSignalRecieved = true;
                sendMessage done = new sendMessage();
                done.execute("received");
                sendingThread sender = new sendingThread();
                sender.start();
                Thread caps = new Thread(new capsLockReceiveThread());
                caps.start();

            } else {
                CardBuilder card = new CardBuilder(MainActivity.this, CardBuilder.Layout.TEXT);
                card.setText("unknown message: " + result);
                setContentView(card.getView());
            }
        }
    }

    class sendMessage extends AsyncTask<String, Object, Object> {
        @Override
        protected Object doInBackground(String... params) {
            try {

                DatagramSocket socket = new DatagramSocket();
                byte[] buf = params[0].getBytes();
                InetAddress hostAddress = InetAddress.getByName(ADDRESS);
                DatagramPacket dgp = new DatagramPacket(buf, buf.length, hostAddress, PORT);
                socket.send(dgp);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    class sendingThread extends Thread {
        private int totalCapsTime = 90000;
        private ArrayList<Integer> capsElapsedTimes;
        private ArrayList<Integer> sleepElapsedTimes;

        public sendingThread() {
            capsElapsedTimes = new ArrayList<>();
            while (totalCapsTime != 0) {
                if (totalCapsTime / 4 > 5000) {
                    Random random = new Random();
                    int low = 5000;
                    int high = totalCapsTime / 4;
                    int r = random.nextInt(high - low) + low;
                    capsElapsedTimes.add(r);
                    totalCapsTime -= r;
                } else {
                    capsElapsedTimes.add(totalCapsTime);
                    totalCapsTime = 0;
                }
            }
            sleepElapsedTimes = (ArrayList) capsElapsedTimes.clone();
        }

        public void run() {
            Random random = new Random();
            while (capsElapsedTimes.size() != 0) {
                try {
                    int r;
                    if (capsElapsedTimes.size() > 1)
                        r = random.nextInt(capsElapsedTimes.size() - 1);
                    else
                        r = 0;
                    AsyncTask on = new sendAction();
                    Log.d("debug", "on for " + capsElapsedTimes.get(r));
                    on.execute(ADDRESS, PORT);
                    sleep(capsElapsedTimes.get(r));
                    capsElapsedTimes.remove(r);
                    AsyncTask off = new sendAction();
                    Random sleepRandomize = new Random();
                    int index;
                    if (sleepElapsedTimes.size() > 1)
                        index = sleepRandomize.nextInt(sleepElapsedTimes.size() - 1);
                    else
                        index = 0;
                    Log.d("debug", "off for " + sleepElapsedTimes.get(index));
                    off.execute(ADDRESS, PORT);
                    sleep(sleepElapsedTimes.get(index));
                    sleepElapsedTimes.remove(index);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            sendMessage sendDone = new sendMessage();
            sendDone.execute("time finished");
            CardBuilder card = new CardBuilder(MainActivity.this, CardBuilder.Layout.TEXT);
            card.setText("time finished");
            setContentView(card.getView());

        }

        public ArrayList<Integer> getCapsElapsedTimes() {
            return capsElapsedTimes;
        }
    }

    class capsLockReceiveThread implements Runnable {

        @Override
        public void run() {
            try {
                byte[] buf = new byte[1000];
                DatagramSocket socket = new DatagramSocket(34144);
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                while (true) {
                    socket.receive(datagramPacket);
                    final String result = new String(datagramPacket.getData(), 0, datagramPacket.getLength());
                    final ImageView img = (ImageView) findViewById(R.id.imageView6);
                    if (firstTime) {
                        MainActivity.this.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                img.setImageResource(result.equals("good") ? R.drawable.thumb_positive : R.drawable.thumb_negative);
                                firstTime = false;
                            }
                        });
                    } else {
                        if (result.equals("good") != good) {
                            good = !good;
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    img.setImageResource(result.equals("good") ? R.drawable.thumb_positive : R.drawable.thumb_negative);
                                }
                            });

                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

}
