package org.iotope.nfc.blog.googleiopart1;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.*;
import android.nfc.tech.MifareUltralight;
import android.nfc.tech.NfcF;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.IOException;
import java.nio.ByteBuffer;


public class IOExample1Activity extends Activity {

    NfcManager nfcManager;
    NfcAdapter nfcAdapter;

    private PendingIntent mPendingIntent;
    private IntentFilter[] mFilters;

    private TextView counter;
    private int result;


    private void setupNfcFilters() {
        mPendingIntent = PendingIntent.getActivity(this, 0, new Intent(this, getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);

        IntentFilter tag = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        mFilters = new IntentFilter[]{tag};
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        nfcManager = (NfcManager) getSystemService(NFC_SERVICE);
        nfcAdapter = nfcManager.getDefaultAdapter();

        counter = (TextView) findViewById(R.id.txt_counter);

        setupNfcFilters();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nfcAdapter != null)
            nfcAdapter.enableForegroundDispatch(this, mPendingIntent, mFilters, null);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (nfcAdapter != null)
            nfcAdapter.disableForegroundDispatch(this);
    }

    @Override
    public void onNewIntent(Intent intent) {
        Log.i("Foreground dispatch", "Discovered tag with intent: " + intent);

        Bundle bundle = intent.getExtras();
        final Tag tag = (Tag) bundle.get("android.nfc.extra.TAG");

        MifareUltralight ultraC = MifareUltralight.get(tag);
        try {
            ultraC.connect();
            byte[] buffer = ultraC.readPages(0x26);
            ultraC.writePage(0x26, plusOne(buffer));
            ultraC.close();

            counter.setText(String.valueOf(result));
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private byte[] plusOne(byte[] in) {
        ByteBuffer buffer = ByteBuffer.wrap(in);
        result = buffer.getInt() + 1;
        buffer.rewind();
        buffer.putInt(result);

        byte[] write = new byte[4];
        buffer.rewind();
        buffer.get(write);

        return write;
    }
}
