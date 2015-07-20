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
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Random;

public class MainActivity extends Activity {


    private final static int PORT = 4747;
    //    private static String ADDRESS = "192.168.0.100";
    private static String ADDRESS = "137.250.171.235";
//    private static String ADDRESS = "-1";

    private static String message = "on";


    private boolean good;
    private boolean firstTime = true;
    private boolean on;
    private String state;
    private int numberOfImportantThings = 3;

    ImageSwitcher imageSwitcher_caps;
    ImageSwitcher imageSwitcher;

    Animation slide_in_left, slide_out_right;
    private boolean finished;
    private boolean sleep;

    DatagramSocket sendMessageSocket;
    byte[] sendMessageBuf;
    InetAddress sendMessageHostAddress;
    DatagramPacket sendMessageDGP;
    private sendingThread sender;

    double end;
    private int layout;
    private double start;
    private double timeSlept;
    private ArrayList<Long> sleepBuffer;


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        try {
            sendMessageSocket = new DatagramSocket();
            sendMessageHostAddress = InetAddress.getByName(ADDRESS);
        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

//        if (ADDRESS.equals("-1")) {
//            setContentView(buildView(R.drawable.wifi, "Scanning network for the server", "Please wait for the glass to find the server"));
//            AsyncTask scanning = new scanningTask();
//            scanning.execute();
//        } else {
//            restartApp(this);
//        }
        setContentView(buildView(R.drawable.sand_clock, "Waiting for start signal", "Start typing to automatically send the start signal"));
        AsyncTask receiver = new receiveStartSignal();
        receiver.execute();
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
    }

    private View buildView(int id, String text, String footnote) {
        CardBuilder card = new CardBuilder(this, CardBuilder.Layout.ALERT);
        card.setIcon(id);
        card.setText(text);
        card.setFootnote(footnote);
        return card.getView();
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
            sender = new sendingThread();
            Thread caps = new Thread(new capsLockReceiveThread());
            Log.d("debug", result);
            switch (result) {
                case "bye":
                    System.exit(0);
                    break;
                case "restart":
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            restartApp(MainActivity.this);
                        }
                    }).start();
                    break;
                case "colored":
                    setContentView(R.layout.iconlayout_single_colored);
                    layout = R.layout.iconlayout_single_colored;
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendMessage("received");
                        }
                    }).start();
                    sender.start();
                    caps.start();
                    receiveStartSignal.this.cancel(true);
                    break;
                case "fading":
                    setContentView(R.layout.iconlayout_single_fading);
                    layout = R.layout.iconlayout_single_fading;
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
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendMessage("received");
                        }
                    }).start();
                    sender.start();
                    caps.start();
                    receiveStartSignal.this.cancel(true);
                    break;
                case "double":
                    layout = R.layout.iconlayout_double;
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
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            sendMessage("received");
                        }
                    }).start();
                    sender.start();
                    caps.start();
                    receiveStartSignal.this.cancel(true);
                    break;
                default:
                    MainActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setContentView(buildView(R.drawable.ic_warning_150, "Unknown message", "Please restart the application"));
                        }
                    });

                    break;
            }
        }
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
            try {
                Random random = new Random();
                while (!finished && capsElapsedTimes.size() != 0) {
                    int indexForSleeping;
                    if (capsElapsedTimes.size() > 1)
                        indexForSleeping = random.nextInt(capsElapsedTimes.size() - 1);
                    else
                        indexForSleeping = 0;
                    AsyncTask on = new sendAction();
                    sleepBuffer = new ArrayList<>();
                    start = System.nanoTime();
                    end = capsElapsedTimes.get(indexForSleeping);
                    sleepBuffer.add((long) end);
                    Log.d("debug", "on for " + capsElapsedTimes.get(indexForSleeping));
                    on.execute(ADDRESS, PORT);
                    int i = 0;
                    while (i < sleepBuffer.size() || sleep) {
                        if (sleep)
                            sleep(100);
                        else {
                            if (sleepBuffer.get(i) <= 0)
                                break;
                            sleep(sleepBuffer.get(i));
                            i++;
                        }

                    }
                    capsElapsedTimes.remove(indexForSleeping);
                    on = new sendAction();
                    if (sleepElapsedTimes.size() > 1)
                        indexForSleeping = random.nextInt(sleepElapsedTimes.size() - 1);
                    else
                        indexForSleeping = 0;
                    sleepBuffer = new ArrayList<>();
                    start = System.nanoTime();
                    end = sleepElapsedTimes.get(indexForSleeping);
                    sleepBuffer.add((long) end);
                    Log.d("debug", "off for " + sleepElapsedTimes.get(indexForSleeping));
                    on.execute(ADDRESS, PORT);
                    i = 0;
                    while (i < sleepBuffer.size() || sleep) {
                        if (sleep)
                            sleep(100);
                        else {
                            if (sleepBuffer.get(i) <= 0)
                                break;
                            sleep(sleepBuffer.get(i));
                            i++;
                        }
                    }
                    sleepElapsedTimes.remove(indexForSleeping);
                }

            } catch (InterruptedException e) {
                Log.d("debug", "thread interrupted");
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
                            if (!finished && !sleep)
                                if (firstTime) {
                                    MainActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            switch (state) {
                                                case "double":
                                                    imageSwitcher.setImageResource(R.drawable.thumb_negative);
                                                    break;
                                                case "colored":
                                                    img.setImageResource((on) ? R.drawable.on_square_bad : R.drawable.off_square_bad);
                                                    break;
                                                case "fading":
                                                    imageSwitcher.setImageResource((on) ? R.drawable.on_square_bad : R.drawable.off_square_bad);
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
                                                        imageSwitcher.setImageResource(on ? R.drawable.on_square_bad : R.drawable.off_square_bad);
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
                        case "finish":
                            finished = true;
                            sendMessageSocket.close();
                            sender.interrupt();
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    setContentView(buildView(R.drawable.finish, "Finish Button was clicked", "Please restart or close the test"));
                                }
                            });

                            break;
                        case "sleep":
                            sleep = true;
                            timeSlept = (System.nanoTime() - start) / 1000000.0;
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    findViewById(R.id.topLinearLayout).setAlpha(0.5F);
                                }
                            });
                            break;
                        case "wakeup":
                            end -= timeSlept;
                            if (end > 0)
                                sleepBuffer.add((long) end);
                            start = System.nanoTime();
                            sleep = false;
                            MainActivity.this.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    findViewById(R.id.topLinearLayout).setAlpha(1);
                                }
                            });
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

    public void restartApp(final MainActivity act) {
        try {
            sendMessage("restart received");
            String message = "";
            DatagramSocket ackSocket = new DatagramSocket(null);
            ackSocket.setReuseAddress(true);
            ackSocket.bind(new InetSocketAddress("137.250.171.64", PORT));
            byte[] ackBuf = new byte[1000];
            DatagramPacket dgp = new DatagramPacket(ackBuf, ackBuf.length);
            Log.d("debug", message);
            while (!message.equals("ack received")) {
                sendMessage("restart received");
                ackSocket.receive(dgp);
                message = new String(dgp.getData(), 0, dgp.getLength());
                Log.d("debug", message);
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

    public void sendMessage(String message) {
        try {
            sendMessageBuf = message.getBytes();
            sendMessageDGP = new DatagramPacket(sendMessageBuf, sendMessageBuf.length, sendMessageHostAddress, PORT);
            sendMessageSocket.send(sendMessageDGP);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public String printReachableHosts(InetAddress inetAddress) {
        String ipAddress = inetAddress.toString();
        ipAddress = ipAddress.substring(1, ipAddress.lastIndexOf('.')) + ".";
        for (int i = 0; i < 256; i++) {
            String otherAddress = ipAddress + String.valueOf(i);
            try {
                if (InetAddress.getByName(otherAddress.toString()).isReachable(50)) {
                    if (!otherAddress.toString().endsWith(".1") && !otherAddress.toString().endsWith(".105")) {
                        ADDRESS = otherAddress.toString();
                        Log.d("address", otherAddress);
                        sendMessageHostAddress = InetAddress.getByName(ADDRESS);
                        return ADDRESS;
                    }
                }

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (ADDRESS.equals("-1"))
            try {
                printReachableHosts(InetAddress.getByName("192.168.0.0"));
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        return ADDRESS;
    }

    class scanningTask extends AsyncTask<Object, String, String> {
        @Override
        protected String doInBackground(Object... params) {
            try {
                Log.d("address", "start searching");
                return printReachableHosts(InetAddress.getByName("192.168.0.0"));

            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            Log.d("address", "searching ended");
            setContentView(buildView(R.drawable.sand_clock, "Waiting for start signal", "Start typing to automatically send the start signal"));
            AsyncTask receiver = new receiveStartSignal();
            receiver.execute();
        }
    }


}
