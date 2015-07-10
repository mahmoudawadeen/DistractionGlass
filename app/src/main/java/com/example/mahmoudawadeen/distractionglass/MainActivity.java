package com.example.mahmoudawadeen.distractionglass;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.ViewSwitcher.ViewFactory;

import com.google.android.glass.widget.CardBuilder;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {


    private final static int PORT = 4747;
    private final static String ADDRESS = "137.250.171.235";

    private static String message = "on";


    private boolean good;
    private boolean firstTime = true;
    private boolean on;
    private String state;
    private int numberOfImportantThings = 3;

    ImageSwitcher imageSwitcher_caps;
    ImageSwitcher imageSwitcher;

    Animation slide_in_left, slide_out_right;


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(buildView());
        AsyncTask receiver = new receiveStartSignal();
        receiver.execute("hey");
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

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
            sendMessage(message);
            message = (message.equals("on") ? "off" : "on");
            return (message.equals("on") ? "off" : "on");
        }

        @Override
        protected void onPostExecute(Object result) {
            if (state.equals("double"))
                imageSwitcher_caps.setImageResource(result.equals("on") ? R.drawable.on_square_white : R.drawable.off_square_white);
            if (state.equals("colored")) {
                ImageView img = (ImageView) findViewById(R.id.imageView);
                img.setImageResource((result.equals("on") ? R.drawable.on_square_bad : R.drawable.off_square_bad));
            }
            if (state.equals("colored") || state.equals("fading"))
                on = result.equals("on");
        }
    }

    class receiveStartSignal extends AsyncTask<Object, Object, String> {

        @Override
        protected String doInBackground(Object[] params) {
            try {
                Log.d("debug", "starting to receive");
                byte[] buf = new byte[1000];
                DatagramSocket socket = new DatagramSocket(34144);
                DatagramPacket datagramPacket = new DatagramPacket(buf, buf.length);
                socket.receive(datagramPacket);
                socket.close();
                return new String(datagramPacket.getData(), 0, datagramPacket.getLength());
            } catch (SocketException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            state = result;
            switch (result) {
                case "bye":
                    System.exit(0);
                    break;
                case "restart":
                    Thread thread = new Thread(new Runnable() {
                        @Override
                        public void run() {
                            restartApp(MainActivity.this);
                        }
                    });
                    thread.start();
                    break;
                case "colored":
                    setContentView(R.layout.iconlayout_single_colored);
                    break;
                case "fading":
                    setContentView(R.layout.iconlayout_single_fading);
                    imageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher);

                    slide_in_left = AnimationUtils.loadAnimation(MainActivity.this,
                            android.R.anim.fade_in);
                    slide_in_left.setDuration(500);
                    slide_out_right = AnimationUtils.loadAnimation(MainActivity.this,
                            android.R.anim.fade_out);
                    slide_out_right.setDuration(500);


                    imageSwitcher.setInAnimation(slide_in_left);
                    imageSwitcher.setOutAnimation(slide_out_right);


                    imageSwitcher.setFactory(new ViewFactory() {
                        @Override
                        public View makeView() {

                            ImageView imageView = new ImageView(MainActivity.this);
                            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                            LayoutParams params = new ImageSwitcher.LayoutParams(
                                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

                            imageView.setLayoutParams(params);
                            return imageView;

                        }
                    });
                    break;
                case "double":
                    setContentView(R.layout.iconlayout_double);
                    imageSwitcher_caps = (ImageSwitcher) findViewById(R.id.imageSwitcher);
                    imageSwitcher = (ImageSwitcher) findViewById(R.id.imageSwitcher2);

                    slide_in_left = AnimationUtils.loadAnimation(MainActivity.this,
                            android.R.anim.fade_in);
                    slide_in_left.setDuration(500);
                    slide_out_right = AnimationUtils.loadAnimation(MainActivity.this,
                            android.R.anim.fade_out);
                    slide_out_right.setDuration(500);


                    imageSwitcher_caps.setInAnimation(slide_in_left);
                    imageSwitcher_caps.setOutAnimation(slide_out_right);

                    imageSwitcher.setInAnimation(slide_in_left);
                    imageSwitcher.setOutAnimation(slide_out_right);

                    imageSwitcher_caps.setFactory(new ViewFactory() {
                        @Override
                        public View makeView() {

                            ImageView imageView = new ImageView(MainActivity.this);
                            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                            LayoutParams params = new ImageSwitcher.LayoutParams(
                                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

                            imageView.setLayoutParams(params);
                            return imageView;

                        }
                    });
                    imageSwitcher.setFactory(new ViewFactory() {
                        @Override
                        public View makeView() {

                            ImageView imageView = new ImageView(MainActivity.this);
                            imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);

                            LayoutParams params = new ImageSwitcher.LayoutParams(
                                    LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

                            imageView.setLayoutParams(params);
                            return imageView;

                        }
                    });
                    break;
//                default:
//                    CardBuilder card = new CardBuilder(MainActivity.this, CardBuilder.Layout.TEXT);
//                    card.setText("unknown message: " + result);
//                    Log.d("debugging", "men hena"+result);
//                    setContentView(card.getView());
//
//                    break;
            }
            sendMessage done = new sendMessage();
            done.execute("received");
            sendingThread sender = new sendingThread();
            sender.start();
            Thread caps = new Thread(new capsLockReceiveThread());
            caps.start();
            receiveStartSignal.this.cancel(true);
        }
    }

    class sendMessage extends AsyncTask<String, Object, Object> {
        @Override
        protected Object doInBackground(String... params) {
            sendMessage(params[0]);
            return null;
        }
    }

    class sendingThread extends Thread {
        private int totalCapsTime = 90000;
        private ArrayList<Integer> capsElapsedTimes;
        private ArrayList<Integer> sleepElapsedTimes;

        public sendingThread() {
            capsElapsedTimes = new ArrayList<>();
            if (numberOfImportantThings == 0) {
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
            } else {
                Random random = new Random();
                int low = 5000;
                int high = 15000;
                while (numberOfImportantThings > 0) {
                    int r = random.nextInt(high - low) + low;
                    capsElapsedTimes.add(r);
                    numberOfImportantThings--;
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

                    final ImageView img = (ImageView) findViewById(R.id.imageView);
                    switch (result) {
                        case "good":
                        case "bad":
                            if (firstTime) {
                                MainActivity.this.runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        switch (state) {
                                            case "double":
                                                imageSwitcher.setImageResource((result.equals("good")) ? R.drawable.thumb_positive : R.drawable.thumb_negative);
                                                break;
                                            case "colored":
                                                img.setImageResource(result.equals("good") ? (on) ? R.drawable.on_square_good : R.drawable.off_square_good : (on)
                                                        ? R.drawable.on_square_bad : R.drawable.off_square_bad);
                                                break;
                                            case "fading":
                                                imageSwitcher.setImageResource((on) ? R.drawable.on_square_white : R.drawable.off_square_white);
                                                break;
                                        }
                                    }

                                });
                                if (state.equals("fading") && result.equals("good")) {
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            imageSwitcher.setImageResource(R.drawable.empty);
                                        }
                                    });
                                }
                                firstTime = false;
                            } else {
                                if (result.equals("good") != good) {
                                    good = !good;
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            switch (state) {
                                                case "double":
                                                    imageSwitcher.setImageResource((good) ? R.drawable.thumb_positive : R.drawable.thumb_negative);
                                                    break;
                                                case "colored":
                                                    img.setImageResource(result.equals("good") ? (on) ? R.drawable.on_square_good : R.drawable.off_square_good : (on)
                                                            ? R.drawable.on_square_bad : R.drawable.off_square_bad);
                                                    break;
                                                case "fading":
                                                    imageSwitcher.setImageResource(on ? R.drawable.on_square_white : R.drawable.off_square_white);
                                                    break;
                                            }
                                        }
                                    });
                                    if (state.equals("fading") && result.equals("good")) {
                                        MainActivity.this.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                imageSwitcher.setImageResource(R.drawable.empty);
                                            }
                                        });
                                    }
                                }
                            }
                            break;
                        case "bye":
                            System.exit(0);
                            break;
                        case "restart":
                            restartApp(MainActivity.this);
                            break;
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void restartApp(MainActivity act) {
        sendMessage("restart received");
        try {
            String message = "";
            DatagramSocket ackSocket = new DatagramSocket(null);
            ackSocket.setReuseAddress(true);
            ackSocket.bind(new InetSocketAddress("137.250.171.64", PORT));
            byte[] ackBuf = new byte[1000];
            DatagramPacket dgp = new DatagramPacket(ackBuf, ackBuf.length);
            while (!message.equals("ack received")) {
                sendMessage("restart received");
                ackSocket.receive(dgp);
                message = new String(dgp.getData(), 0, dgp.getLength());
                Log.d("debugging", message);
            }
            ackSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Context context = act.getApplicationContext();
        Intent mStartActivity = new Intent(context, MainActivity.class);
        int mPendingIntentId = 123456;
        PendingIntent mPendingIntent = PendingIntent.getActivity(context, mPendingIntentId, mStartActivity, PendingIntent.FLAG_CANCEL_CURRENT);
        AlarmManager mgr = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        mgr.set(AlarmManager.RTC, System.currentTimeMillis() + 100, mPendingIntent);
        System.exit(0);
    }

    public static void sendMessage(String message) {
        try {
            DatagramSocket socket = new DatagramSocket();
            byte[] buf = message.getBytes();
            InetAddress hostAddress = InetAddress.getByName(ADDRESS);
            DatagramPacket dgp = new DatagramPacket(buf, buf.length, hostAddress, PORT);
            socket.send(dgp);
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
