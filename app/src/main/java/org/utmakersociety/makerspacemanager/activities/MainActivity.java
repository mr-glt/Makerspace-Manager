package org.utmakersociety.makerspacemanager.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.Manifest;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.Toast;

import com.budiyev.android.codescanner.CodeScanner;
import com.budiyev.android.codescanner.CodeScannerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.firestore.FirebaseFirestore;
import com.pchmn.materialchips.ChipsInput;
import com.pchmn.materialchips.model.Chip;
import com.pchmn.materialchips.model.ChipInterface;
import com.skyfishjy.library.RippleBackground;

import org.utmakersociety.makerspacemanager.R;
import org.utmakersociety.makerspacemanager.adapters.UsersAdapter;
import org.utmakersociety.makerspacemanager.helpers.RecyclerItemClickListener;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity{
    //QR Code Variables
    CodeScanner codeScanner;
    CodeScannerView scannerView;

    //NFC Variables
    boolean writeMode;
    String dataToWrite = "DATAEMPTY";
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];

    //Firebase
    FirebaseFirestore db;

    //UI
    RecyclerView rv;
    FloatingActionButton button;
    RippleBackground rippleBackground;
    ImageView nfcImage;
    ChipsInput chipsInput;

    //Other
    boolean copyMode;
    Context context;
    View contextView;
    Tag tag;
    List<Chip> utilChips = new ArrayList<>();
    List<Chip> users = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        context = this;
        contextView = findViewById(R.id.mainLayout);


        utilChips.add(new Chip(getDrawable
                (R.drawable.baseline_memory_24),"EECS","EECS Major"));
        utilChips.add(new Chip(getDrawable
                (R.drawable.baseline_developer_board_24),"ENGT","Engineering Tech Major"));
        utilChips.add(new Chip(getDrawable
                (R.drawable.baseline_terrain_24),"CIVE","CIVE Major"));
        utilChips.add(new Chip(getDrawable
                (R.drawable.baseline_bug_report_24),"BIOE","BIOE Major"));
        utilChips.add(new Chip(getDrawable
                (R.drawable.baseline_build_24),"MECHE","MECHE Major"));
        utilChips.add(new Chip(getDrawable
                (R.drawable.baseline_opacity_24),"CHEME","CHEME Major"));
        utilChips.add(new Chip(getDrawable
                (R.drawable.baseline_book_24),"Other","Other Major"));

        utilChips.add(new Chip(getDrawable
                (R.drawable.baseline_vpn_key_24),getString(R.string.admin),"Has authority over the database"));
        utilChips.add(new Chip(getDrawable
                (R.drawable.baseline_assignment_24),getString(R.string.certifier)
                ,"Has authority certification level"));
        utilChips.add(new Chip(getDrawable
                (R.drawable.baseline_work_24),getString(R.string.employee)
                ,"Employed to manage the Makerspace"));
        utilChips.add(new Chip(getDrawable
                (R.drawable.baseline_child_friendly_24),getString(R.string.freshman_design)
                ,"Member of freshman design"));
        utilChips.add(new Chip(getDrawable
                (R.drawable.baseline_school_24),getString(R.string.senior_design)
                ,"Member of senior design"));
        utilChips.add(new Chip(getDrawable
                (R.drawable.baseline_account_balance_24),getString(R.string.student_organization)
                ,"Representative of another student organization"));

        //Firebase
        db = FirebaseFirestore.getInstance();
        db.collection("users")
                .orderBy("name")
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        users.clear();
                        for (int i = 0; i < task.getResult().size();i++){
                            users.add(new Chip(Objects.requireNonNull(task.getResult().
                                    getDocuments().get(i).get("name")).toString()
                                    ,"Level " + Objects.requireNonNull(task.getResult()
                                    .getDocuments().get(i).get("certLevel")).toString() + " Member"));
                        }
                        List<Chip> chips = new ArrayList<>(utilChips);
                        chips.addAll(users);
                        chipsInput.setFilterableList(chips);
                        rv.setLayoutManager(new LinearLayoutManager(getBaseContext()));
                        rv.setAdapter(new UsersAdapter(task.getResult(),context));
                        rv.addOnItemTouchListener(new RecyclerItemClickListener(getBaseContext(),
                                (view, position) -> {
                        }));
                    }
                });

        //Setup UI
        button = findViewById(R.id.floatingActionButton);
        scannerView = findViewById(R.id.scanner_view);
        rv = findViewById(R.id.recView);
        chipsInput = findViewById(R.id.chips_input);
        rippleBackground = findViewById(R.id.rippleView);
        nfcImage = findViewById(R.id.nfcImage);
        button.setOnClickListener(view -> {
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED){
                ActivityCompat.requestPermissions(this, new String[] {Manifest.permission.CAMERA}, 1);
            }else{
                if (scannerView.getVisibility()==View.VISIBLE){
                    scannerView.setVisibility(View.GONE);
                    codeScanner.stopPreview();
                    button.setImageDrawable(getResources().getDrawable(
                            R.drawable.baseline_photo_camera_white_24, context.getTheme()));
                }else{
                    scannerView.setVisibility(View.VISIBLE);
                    codeScanner.startPreview();
                    button.setImageDrawable(getResources().getDrawable(
                            R.drawable.baseline_close_white_24, context.getTheme()));
                }
            }
        });

        chipsInput.addChipsListener(new ChipsInput.ChipsListener() {
            @Override
            public void onChipAdded(ChipInterface chip, int newSize) {
                Log.e("CHIPZ","Added: " + chip.getLabel());
            }

            @Override
            public void onChipRemoved(ChipInterface chip, int newSize) {

            }

            @Override
            public void onTextChanged(CharSequence text) {
            }
        });

        //QR Code Scanner
        codeScanner = new CodeScanner(this, scannerView);
        scannerView.setVisibility(View.GONE);
        codeScanner.stopPreview();
        codeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            dataToWrite = result.getText();
            button.setImageDrawable(getResources().getDrawable(
                    R.drawable.baseline_photo_camera_white_24, context.getTheme()));
            if (!copyMode){
                runUser(result.getText());
            }else{
                nfcImage.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.baseline_nfc_24));
                rippleBackground.startRippleAnimation();
            }
            scannerView.setVisibility(View.GONE);
            codeScanner.stopPreview();
        }));
        scannerView.setOnClickListener(view -> codeScanner.startPreview());

        //NFC
        readFromIntent(getIntent());
        nfcAdapter = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapter == null) {
            Toast.makeText(this, "This device doesn't support NFC.", Toast.LENGTH_LONG).show();
            finish();
        }
        pendingIntent = PendingIntent.getActivity(this, 0, new Intent(this,
                getClass()).addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP), 0);
        IntentFilter tagDetected = new IntentFilter(NfcAdapter.ACTION_TAG_DISCOVERED);
        tagDetected.addCategory(Intent.CATEGORY_DEFAULT);
        writeTagFilters = new IntentFilter[] { tagDetected };

    }
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[],
                                           int[] grantResults) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_DENIED){
            Snackbar.make(contextView,
                    R.string.PERM_GRANT_ERROR, Snackbar.LENGTH_SHORT)
                    .show();
        }else{
            scannerView.setVisibility(View.VISIBLE);
            codeScanner.startPreview();
            button.setImageDrawable(getResources().getDrawable(
                    R.drawable.baseline_close_white_24, context.getTheme()));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.copy_write:
                copyMode = !copyMode;
                if (copyMode){
                    rv.setVisibility(View.GONE);
                    rippleBackground.setVisibility(View.VISIBLE);
                    rippleBackground.startRippleAnimation();
                    nfcImage.setImageDrawable(ContextCompat.getDrawable(this,
                            R.drawable.baseline_photo_camera_24));
                }else{
                    rv.setVisibility(View.VISIBLE);
                    rippleBackground.setVisibility(View.GONE);
                    rippleBackground.stopRippleAnimation();
                }
                Snackbar.make(contextView,
                        "Write mode set to: " + copyMode, Snackbar.LENGTH_SHORT)
                        .show();

                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        setIntent(intent);
        readFromIntent(intent);
        if(NfcAdapter.ACTION_TAG_DISCOVERED.equals(intent.getAction())){
            tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        WriteModeOn();
        codeScanner.startPreview();
    }

    @Override
    protected void onPause() {
        codeScanner.releaseResources();
        WriteModeOff();
        super.onPause();
    }

    private void runUser(String key){
        db.collection("users").document(key)
                .get()
                .addOnCompleteListener(task -> {
                    if (!task.getResult().exists()){
                        Intent intent = new Intent(context, NewUser.class);
                        intent.putExtra("GUID", key);
                        startActivity(intent);
                    }else{
                        Intent intent = new Intent(context, ExistingUser.class);
                        intent.putExtra("GUID", key);
                        startActivity(intent);
                    }
                });
    }

    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        if (NfcAdapter.ACTION_TAG_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)
                || NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            Parcelable[] rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES);
            NdefMessage[] msgs = null;
            if (rawMsgs != null) {
                msgs = new NdefMessage[rawMsgs.length];
                for (int i = 0; i < rawMsgs.length; i++) {
                    msgs[i] = (NdefMessage) rawMsgs[i];
                }
            }
            buildTagViews(msgs);
        }
    }

    private void buildTagViews(NdefMessage[] msgs) {
        if (msgs == null || msgs.length == 0) return;

        String text = "";
        //String tagId = new String(msgs[0].getRecords()[0].getType());
        byte[] payload = msgs[0].getRecords()[0].getPayload();
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16"; // Get the Text Encoding
        int languageCodeLength = payload[0] & 0063; // Get the Language Code, e.g. "en"
        // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
        try {
            text = new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        } catch (UnsupportedEncodingException e) {
            Log.e("UnsupportedEncoding", e.toString());
        }
        if (copyMode){
            try {
                if(tag == null) {
                    Snackbar.make(contextView,
                            R.string.NFC_ERROR_DETECTED, Snackbar.LENGTH_SHORT)
                            .show();
                } else {
                    write(dataToWrite, tag);
                    Snackbar.make(contextView,
                            R.string.NFC_WRITE_SUCCESS, Snackbar.LENGTH_SHORT)
                            .show();
                    nfcImage.setImageDrawable(getResources().getDrawable(
                            R.drawable.baseline_check_circle_24, context.getTheme()));
                    rippleBackground.stopRippleAnimation();
                }
            } catch (IOException | FormatException e) {
                Snackbar.make(contextView,
                        R.string.NFC_WRITE_ERROR, Snackbar.LENGTH_SHORT)
                        .show();
                e.printStackTrace();
            }
        }else{
            runUser(text);
        }
    }

    private void write(String text, Tag tag) throws IOException, FormatException {
        NdefRecord[] records = { createRecord(text) };
        NdefMessage message = new NdefMessage(records);
        Ndef ndef = Ndef.get(tag);
        ndef.connect();
        ndef.writeNdefMessage(message);
        ndef.close();
    }

    private NdefRecord createRecord(String text) throws UnsupportedEncodingException {
        String lang       = "en";
        byte[] textBytes  = text.getBytes();
        byte[] langBytes  = lang.getBytes("US-ASCII");
        int    langLength = langBytes.length;
        int    textLength = textBytes.length;
        byte[] payload    = new byte[1 + langLength + textLength];

        payload[0] = (byte) langLength;

        System.arraycopy(langBytes, 0, payload, 1,              langLength);
        System.arraycopy(textBytes, 0, payload, 1 + langLength, textLength);

        NdefRecord recordNFC = new NdefRecord(NdefRecord.TNF_WELL_KNOWN,  NdefRecord.RTD_TEXT,  new byte[0], payload);

        return recordNFC;
    }

    private void WriteModeOn(){
        writeMode = true;
        nfcAdapter.enableForegroundDispatch(this, pendingIntent, writeTagFilters, null);
    }

    private void WriteModeOff(){
        writeMode = false;
        nfcAdapter.disableForegroundDispatch(this);
    }
}
