package org.utmakersociety.makerspacemanager.activities;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.FormatException;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.firestore.FirebaseFirestore;
import com.skyfishjy.library.RippleBackground;

import org.utmakersociety.makerspacemanager.R;
import org.utmakersociety.makerspacemanager.activities.MainActivity;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class NewUser extends AppCompatActivity {
    //UI
    TextInputEditText nameET;
    TextInputEditText rocketNumberET;
    TextInputEditText studentOrgET;
    TextInputLayout orgHolder;
    CheckBox employeeCB;
    CheckBox freshmanDesignCB;
    CheckBox seniorDesignCB;
    CheckBox studentOrgCB;
    ImageButton writeTag;
    Button addUser;
    RippleBackground rippleBackground;
    ImageView nfcImage;
    TextView nfcWriteText;

    //NFC
    NfcAdapter nfcAdapter;
    PendingIntent pendingIntent;
    IntentFilter writeTagFilters[];
    boolean writeMode;
    boolean firstOpen = true;

    //Firebase
    FirebaseFirestore db;

    //Other
    String guid;
    String major = "Other";
    Context context;
    Tag tag;
    View contextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_new_user);
        context = this;
        contextView = findViewById(R.id.layout);

        //Get GUID passed from activity
        Intent intent = getIntent();
        guid = intent.getStringExtra("GUID");

        //Firebase
        db = FirebaseFirestore.getInstance();

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

        //Spinner
        Spinner spinner = findViewById(R.id.spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.majors_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                switch (i) {
                    case 0:
                        major = "Other";
                        break;
                    case 1:
                        major = "EECS";
                        break;
                    case 2:
                        major = "BIOE";
                        break;
                    case 4:
                        major = "MIME";
                        break;
                    case 5:
                        major = "CHEME";
                        break;
                    case 6:
                        major = "CIVE";
                        break;
                    case 7:
                        major = "ENGT";
                        break;
                    default:
                        major = "Other";
                        break;
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });

        //UI
        addUser = findViewById(R.id.addUser);
        writeTag = findViewById(R.id.burnTag);
        nameET = findViewById(R.id.name);
        rocketNumberET = findViewById(R.id.rocketNumber);
        employeeCB = findViewById(R.id.employeeCB);
        freshmanDesignCB = findViewById(R.id.freshmanDesignCB);
        seniorDesignCB = findViewById(R.id.seniorDesignCB);
        studentOrgCB = findViewById(R.id.studentOrgCB);
        studentOrgET = findViewById(R.id.studentOrgTitle);
        orgHolder = findViewById(R.id.orgHolder);
        rippleBackground = findViewById(R.id.rippleView);
        nfcImage = findViewById(R.id.nfcImage);
        nfcWriteText = findViewById(R.id.nfcWriteText);
        rippleBackground.startRippleAnimation();

        writeTag.setOnClickListener(view -> {
            try {
                if(tag == null) {
                    if (!firstOpen){
                        Snackbar.make(contextView,
                                R.string.NFC_ERROR_DETECTED, Snackbar.LENGTH_SHORT)
                                .show();
                    }else{
                        firstOpen = false;
                    }
                } else {
                    write(guid, tag);
                    Snackbar.make(contextView,
                            R.string.NFC_WRITE_SUCCESS, Snackbar.LENGTH_SHORT)
                            .show();
                    addUser.setEnabled(true);
                }
            } catch (IOException | FormatException e) {
                Snackbar.make(contextView,
                        R.string.NFC_WRITE_ERROR, Snackbar.LENGTH_SHORT)
                        .show();
                e.printStackTrace();
            }
        });

        studentOrgCB.setOnCheckedChangeListener((compoundButton, b) -> {
            if (b){
                orgHolder.setVisibility(View.VISIBLE);
            }else{
                orgHolder.setVisibility(View.GONE);
            }
        });

        addUser.setOnClickListener(view -> {
            boolean allGood = true;
            Map<String, Object> user = new HashMap<>();

            if (!Objects.requireNonNull(nameET.getText()).toString().equals(""))
                user.put("name", nameET.getText().toString());
            else{
                nameET.setError("Must have a name.");
                allGood = false;
            }

            if (!Objects.requireNonNull(rocketNumberET.getText()).toString().equals(""))
                user.put("rocketNumber",rocketNumberET.getText().toString());
            else{
                rocketNumberET.setError("Must have a rocket number.");
                allGood = false;
            }
            user.put("employee",employeeCB.isChecked());
            user.put("freshmanDesign",freshmanDesignCB.isChecked());
            user.put("seniorDesign",seniorDesignCB.isChecked());
            user.put("studentOrg",studentOrgCB.isChecked());

            user.put("ms",false);
            user.put("admin",false);
            user.put("cert",false);
            user.put("certLevel","1");


            if (studentOrgCB.isChecked()){
                if (!Objects.requireNonNull(studentOrgET.getText()).toString().equals(""))
                    user.put("studentOrgName",studentOrgET.getText().toString());
                else{
                    studentOrgET.setError("Must have a name.");
                    allGood = false;
                }
            }

            user.put("major",major);

            if (allGood){
                Intent mainAct = new Intent(context, MainActivity.class);
                db.collection("users").document(guid)
                    .set(user)
                    .addOnSuccessListener(aVoid -> startActivity(mainAct))
                    .addOnFailureListener(e -> Snackbar.make(contextView,
                            R.string.DATABASE_ADD_ERROR, Snackbar.LENGTH_SHORT)
                            .show());
            }
        });
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
    }

    @Override
    protected void onPause() {
        WriteModeOff();
        super.onPause();
    }

    private void readFromIntent(Intent intent) {
        String action = intent.getAction();
        try {
            if(tag == null) {
                if (!firstOpen){
                    Snackbar.make(contextView,
                            R.string.NFC_ERROR_DETECTED, Snackbar.LENGTH_SHORT)
                            .show();
                }else{
                    firstOpen = false;
                }
            } else {
                write(guid, tag);
                Snackbar.make(contextView,
                        R.string.NFC_WRITE_SUCCESS, Snackbar.LENGTH_SHORT)
                        .show();
                rippleBackground.stopRippleAnimation();
                nfcImage.setImageDrawable(ContextCompat.getDrawable(this,
                        R.drawable.baseline_check_circle_24));
                nfcWriteText.setText(R.string.nfc_good);
                addUser.setEnabled(true);
            }
        } catch (IOException | FormatException e) {
            Snackbar.make(contextView,
                    R.string.NFC_WRITE_ERROR, Snackbar.LENGTH_SHORT)
                    .show();
            e.printStackTrace();
        }
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
