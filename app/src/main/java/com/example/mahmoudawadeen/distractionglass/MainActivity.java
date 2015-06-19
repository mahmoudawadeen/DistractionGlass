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
import android.widget.AdapterView;

import java.io.Console;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;

/**
 * An {@link Activity} showing a tuggable "Hello World!" card.
 * <p/>
 * The main content view is composed of a one-card {@link CardScrollView} that provides tugging
 * feedback to the user when swipe gestures are detected.
 * If your Glassware intends to intercept swipe gestures, you should set the content view directly
 * and use a {@link com.google.android.glass.touchpad.GestureDetector}.
 *
 * @see <a href="https://developers.google.com/glass/develop/gdk/touch">GDK Developer Guide</a>
 */
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

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

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
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.TEXT);
        card.addImage(R.drawable.cap3_off);
//        card.setText("Waiting for start signal");
//        card.setEmbeddedLayout(R.layout.iconlayout);
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
            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return (message.equals("on") ? "off" : "on");
        }

        @Override
        protected void onPostExecute(Object result) {
            CardBuilder card = new CardBuilder(MainActivity.this, CardBuilder.Layout.TEXT);
            card.setText((String) result);
            setContentView(card.getView());
        }
    }

    class receiveStartSignal extends AsyncTask<Object, Object, Boolean> {

        @Override
        protected Boolean doInBackground(Object[] params) {
            try {
                Log.d("debug", "starting to receive");
                byte[] buf = new byte[1000];
                DatagramSocket socket = new DatagramSocket(34144);
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                socket.receive(datagramPacket);
                Log.d("debug", "received");
                return true;
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return false;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            if (result) {
                startSignalRecieved=true;
                sendMessage done = new sendMessage();
                done.execute("received");
                sendingThread sender = new sendingThread();
                sender.start();
            } else {
                CardBuilder card = new CardBuilder(MainActivity.this, CardBuilder.Layout.TEXT);
                card.setText("receiving start signal failed");
                setContentView(card.getView());
            }
        }
    }
    class sendMessage extends AsyncTask<String,Object,Object>{
        @Override
        protected Object doInBackground(String... params) {
            try {

                DatagramSocket socket = new DatagramSocket();
                byte[] buf = params[0].getBytes();
                InetAddress hostAddress = InetAddress.getByName(ADDRESS);
                DatagramPacket dgp = new DatagramPacket(buf, buf.length, hostAddress, PORT);
                socket.send(dgp);
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (UnknownHostException e) {
                e.printStackTrace();
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
                if (totalCapsTime/4 > 5000) {
                    Random random = new Random();
                    int low = 5000;
                    int high = totalCapsTime/4;
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

}
