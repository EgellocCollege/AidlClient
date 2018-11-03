package com.example.client;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Process;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.example.server.IRemoteService;


public class MainActivity extends AppCompatActivity {


    private String TAG = "AIDL CLIENT";


    IRemoteService mService = null;

    Button mKillButton;
    TextView mCallbackText;

    private boolean mIsBound;

    private static final String AIDL_PACKAGE_NAME = "com.example.server";
    private static final String AIDL_SERVER_NAME = "com.example.server.RemoteServer";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Watch for button clicks.
        Button button = (Button)findViewById(R.id.bind);
        button.setOnClickListener(mBindListener);
        button = (Button)findViewById(R.id.unbind);
        button.setOnClickListener(mUnbindListener);
        mKillButton = (Button)findViewById(R.id.kill);
        mKillButton.setEnabled(false);

        mCallbackText = (TextView)findViewById(R.id.callback);
        mCallbackText.setText("Not attached.");

    }

    private  View.OnClickListener mBindListener = new View.OnClickListener() {
        public void onClick(View v) {
            // Establish a couple connections with the service, binding
            // by interface names.  This allows other applications to be
            // installed that replace the remote service by implementing
            // the same interface.


            Log.e(TAG,"Action:" + IRemoteService.class.getName());

            Intent intent = new Intent(IRemoteService.class.getName()); // Intent(String action)



            intent.setClassName(AIDL_PACKAGE_NAME, AIDL_SERVER_NAME);   // 在Android 5.0以后，就不允许使用非特定的Intent来绑定Service了，需要使用如下方法

            bindService(intent , mConnection,  Context.BIND_AUTO_CREATE);

            mIsBound = true;
            mCallbackText.setText("Binding.");
        }
    };

    /**
     * Class for interacting with the main interface of the service.
     */
    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className,
                                       IBinder service) {
            // This is called when the connection with the service has been
            // established, giving us the service object we can use to
            // interact with the service.  We are communicating with our
            // service through an IDL interface, so get a client-side
            // representation of that from the raw service object.
            mService = IRemoteService.Stub.asInterface(service);

            mKillButton.setEnabled(true);
            try {
                mHandler.sendMessage(mHandler.obtainMessage(BUMP_MSG, mService.sum(1,9), 0));
            } catch (RemoteException e) {
                // In this case the service has crashed before we could even
                // do anything with it; we can count on soon being
                // disconnected (and then reconnected if it can be restarted)
                // so there is no need to do anything here.
            }



//            // We want to monitor the service for as long as we are
//            // connected to it.
//            try {
//                mService.registerCallback(mCallback);
//            } catch (RemoteException e) {
//                // In this case the service has crashed before we could even
//                // do anything with it; we can count on soon being
//                // disconnected (and then reconnected if it can be restarted)
//                // so there is no need to do anything here.
//            }

            // As part of the sample, tell the user what happened.
            Toast.makeText(MainActivity.this, R.string.remote_service_connected,
                    Toast.LENGTH_SHORT).show();
        }

        public void onServiceDisconnected(ComponentName className) {
            // This is called when the connection with the service has been
            // unexpectedly disconnected -- that is, its process crashed.
            mService = null;
            mKillButton.setEnabled(false);
            mCallbackText.setText("Disconnected.");

            // As part of the sample, tell the user what happened.
            Toast.makeText(MainActivity.this, R.string.remote_service_disconnected,
                    Toast.LENGTH_SHORT).show();
        }
    };




    private View.OnClickListener mUnbindListener = new View.OnClickListener() {
        public void onClick(View v) {
            if (mIsBound) {
                // If we have received the service, and hence registered with
                // it, then now is the time to unregister.
//                if (mService != null) {
//                    try {
//                        mService.unregisterCallback(mCallback);
//                    } catch (RemoteException e) {
//                        // There is nothing special we need to do if the service
//                        // has crashed.
//                    }
//                }

                // Detach our existing connection.
                unbindService(mConnection);
                mKillButton.setEnabled(false);
                mIsBound = false;
                mCallbackText.setText("Unbinding.");
            }
        }
    };


    // ----------------------------------------------------------------------
    // Code showing how to deal with callbacks.
    // ----------------------------------------------------------------------

    /**
     * This implementation is used to receive callbacks from the remote
     * service.
     */
    private IRemoteService mCallback = new IRemoteService.Stub() {
        @Override
        public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

        }

        @Override
        public int  sum(int a,int b ) throws RemoteException {
            return 0;
        }

        /**
         * This is called by the remote service regularly to tell us about
         * new values.  Note that IPC calls are dispatched through a thread
         * pool running in each process, so the code executing here will
         * NOT be running in our main thread like most other things -- so,
         * to update the UI, we need to use a Handler to hop over there.
         */
        public void valueChanged(int value) {
            mHandler.sendMessage(mHandler.obtainMessage(BUMP_MSG, value, 0));
        }

    };

    private static final int BUMP_MSG = 1;

    private Handler mHandler = new Handler() {
        @Override public void handleMessage(Message msg) {
            switch (msg.what) {
                case BUMP_MSG:
                    mCallbackText.setText("Received from service: " + msg.arg1);
                    break;
                default:
                    super.handleMessage(msg);
            }
        }

    };


}
