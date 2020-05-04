package com.tumblr.dateapp.iloveyou.activities;



import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.media.MediaRecorder;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.storage.StorageReference;
import com.google.gson.Gson;
import com.tumblr.dateapp.iloveyou.adapters.ActiveConversationsAdapter;
import com.tumblr.dateapp.iloveyou.adapters.ChatAdapter;
import com.tumblr.dateapp.iloveyou.constants.Constant;
import com.tumblr.dateapp.iloveyou.models.Message;
import com.tumblr.dateapp.iloveyou.models.NotificationModel;
import com.tumblr.dateapp.iloveyou.models.UserProfile;
import com.tumblr.dateapp.iloveyou.utils.DatabaseUtils;
import com.tumblr.dateapp.iloveyou.utils.FileUtils;
import com.tumblr.dateapp.iloveyou.utils.ImageUtils;
import com.tumblr.dateapp.iloveyou.utils.PermissionUtils;
import com.tumblr.dateapp.iloveyou.R;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;


public class ChatActivity extends AppCompatActivity {

    public static final String PARTNER_USER_PROFILE = "PARTNER_USER_PROFILE";

    private List<UserProfile> conversationProfiles;
    private ActiveConversationsAdapter activeConversationsAdapter;
    private ListView conversationsView;
    private ProgressBar mainProgresBar;
    private AutoCompleteTextView userNameView;

    private static final int CAMERA = 1;
    private static final int GALLERY = 2;
    private static final int RECORD_AUDIO = 3;

    private static final String[] WRITE_EXTERNAL_PERMISSION = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
    private static final String[] CAMERA_PERMISSION = new String[]{Manifest.permission.CAMERA};
    private static final String[] RECORD_AUDIO_PERMISSION = new String[]{Manifest.permission.RECORD_AUDIO};

    private String conversationId;

    private List<Message> messages;

    private ChatAdapter chatAdapter;

    private EditText messageEditView;
    private ImageButton messageSendButton;

//    private UserProfile destinationUserProfile;

    private final TextWatcher editMessageTextWatcher = new TextWatcher() {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {

        }

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {

            if (s.length() == 0) {
                messageSendButton.setEnabled(false);
            } else {
                messageSendButton.setEnabled(true);
            }
        }

        @Override
        public void afterTextChanged(Editable s) {

        }
    };
    private String imagePath;
    private String imageUrl;
    private Uri imageUri;
    private Bitmap resizedImage;
    private ImageButton messageAtachImageButton;
    private ListView messageListView;
    private ProgressBar progressBar;
    private final ChildEventListener messageListener = new ChildEventListener() {
        @Override
        public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            Message message = dataSnapshot.getValue(Message.class);

            if (message != null) {
                chatAdapter.add(message);
                messageListView.setSelection(messages.size() - 1);
            } else {
                Log.w(Constant.NEARBY_CHAT, "No messages");
            }

            if (progressBar.getVisibility() == View.VISIBLE) {
                hideProgressBar();
            }
        }

        @Override
        public void onChildChanged(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onChildRemoved(DataSnapshot dataSnapshot) {
        }

        @Override
        public void onChildMoved(DataSnapshot dataSnapshot, String s) {

        }

        @Override
        public void onCancelled(DatabaseError databaseError) {
            Log.w(Constant.NEARBY_CHAT, "loadPost:onCancelled", databaseError.toException());
        }
    };
    private UserProfile conversationPartner;
    private UserProfile destinationUserProfile;

    private ImageButton messageRecordButton;
    private MediaRecorder mediaRecorder;
    private boolean recording;
    private String recordPath;
    private String recordUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        // spinner

        progressBar = (ProgressBar) findViewById(R.id.chat_spinner);

        conversationPartner = (UserProfile) getIntent().getSerializableExtra(PARTNER_USER_PROFILE);

        messageEditView = (EditText) findViewById(R.id.message_edit);
        messageEditView.addTextChangedListener(editMessageTextWatcher);

        messageSendButton = (ImageButton) findViewById(R.id.message_send);
        messageSendButton.setEnabled(false);
        messageSendButton.setOnClickListener(v -> sendMessage());

        messageAtachImageButton = (ImageButton) findViewById(R.id.message_attach_image);
        messageAtachImageButton.setOnClickListener(v -> showImageAttachementDialog());

        recording = false;
        messageRecordButton = (ImageButton) findViewById(R.id.message_record_audio);
        messageRecordButton.setOnClickListener(v -> voiceRecordingAction());

        messages = new ArrayList<>();

        conversationId = getConversationId(conversationPartner.getId());

        DatabaseUtils.getMessagesByConversationId(conversationId)
                .addChildEventListener(messageListener);

        chatAdapter = new ChatAdapter(this, messages);
        messageListView = (ListView) findViewById(R.id.message_list);
        messageListView.setVisibility(View.GONE);

        messageListView.setAdapter(chatAdapter);

        // set conversation title
        setTitle(conversationPartner.getUserName());

        // hide keyboard by default
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN);
    }





    private void initializeMediaRecord(){
        mediaRecorder = new MediaRecorder();
        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mediaRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB);
    }

    private void sendMessage() {
        Message newMessage = new Message();

        // text message
        if (imageUrl == null && recordUrl == null) {

            String textContent = messageEditView.getText().toString();
            newMessage.setType(Message.Type.TEXT);
            newMessage.setContent(textContent);

            sendGcm();
            messageEditView.setText("");
        }
        // image message
        else if(imageUrl != null) {

            newMessage.setType(Message.Type.IMAGE);
            newMessage.setContent(imageUrl);

            imageUrl = null;
        }
        else if(recordUrl != null){
            newMessage.setType(Message.Type.SOUND);
            newMessage.setContent(recordUrl);

            recordUrl = null;
        }
        else{
            Log.e(Constant.NEARBY_CHAT, "Unknow message type");
        }

        newMessage.setDate(new Date());
        newMessage.setSenderId(DatabaseUtils.getCurrentUUID());

        String id = DatabaseUtils.getMessagesByConversationId(conversationId)
                .push()
                .getKey();

        newMessage.setId(id);

        DatabaseUtils.getMessagesByConversationId(conversationId)
                .child(id)
                .setValue(newMessage);
    }


    void sendGcm() {

        Gson gson = new Gson();

        setTitle(conversationPartner.getUserName());

        String userName = FirebaseAuth.getInstance().getCurrentUser().getDisplayName();
        NotificationModel notificationModel = new NotificationModel();
        notificationModel.to = conversationPartner.pushToken;
        notificationModel.notification.title = conversationPartner.getUserName();
        notificationModel.notification.text = messageEditView.getText().toString();
        notificationModel.data.title = conversationPartner.getUserName();
        notificationModel.data.text = messageEditView.getText().toString();

        RequestBody requestBody = RequestBody.create(MediaType.parse("application/json; charset=utf8"),gson.toJson(notificationModel));

        Request request = new Request.Builder()
                .header("Content-Type", "application/json")
                .addHeader("Authorization", "key=API")
                .url("http://gcm-http.googleapis.com/gcm/send")
                .post(requestBody)
                .build();
        OkHttpClient okHttpClient = new OkHttpClient();
        okHttpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {

            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

            }
        });

    }




    private void sendImage(Bitmap image) {

        StorageReference storageReference = DatabaseUtils.getStorageDatabase().getReference(imagePath);
        DatabaseUtils.savePictureOnline(image, storageReference, taskSnapshot -> {
            Log.w(Constant.NEARBY_CHAT, "이미지가 업로드 됐습니다. 메세지를 보냅니다.");
            // send a image message
            storageReference.getDownloadUrl().addOnSuccessListener(e -> {
                imageUrl = e.toString();
                sendMessage();
            });

        }, e -> Log.w(Constant.NEARBY_CHAT, e.getMessage()));
    }

    private void sendRecord(){
        StorageReference storageReference = DatabaseUtils.getStorageDatabase().getReference(recordPath);
        DatabaseUtils.saveRecordOnline(recordPath, storageReference, taskSnapshot -> {
            storageReference.getDownloadUrl().addOnSuccessListener(e -> {
                recordUrl = e.toString();
                sendMessage();
            });
        }, e -> Log.w(Constant.NEARBY_CHAT, e.getMessage()));

    }

    private void showImageAttachementDialog() {
        AlertDialog.Builder pictureDialog = new AlertDialog.Builder(this);
        pictureDialog.setTitle("Select Action");
        String[] pictureDialogItems = {
                "사진",
                "카메라"};
        pictureDialog.setItems(pictureDialogItems,
                (dialog, which) -> {
                    switch (which) {
                        case 0:
                            choosePhotoFromGallery();
                            break;
                        case 1:
                            takePhotoFromCamera();
                            break;
                    }
                });
        pictureDialog.show();
    }

    public void choosePhotoFromGallery() {

        boolean isAndroidVersionNew = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
        if (isAndroidVersionNew) {
            if (!PermissionUtils.hasWritePermission(this)) {
                ActivityCompat.requestPermissions(this, WRITE_EXTERNAL_PERMISSION, GALLERY);
            }
        }

        if (!isAndroidVersionNew || PermissionUtils.hasWritePermission(this)) {

            Intent galleryIntent = new Intent(Intent.ACTION_PICK,
                    android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            startActivityForResult(galleryIntent, GALLERY);
        }
    }

    private void takePhotoFromCamera() {

        boolean isAndroidVersionNew = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
        if (isAndroidVersionNew) {
            if (!PermissionUtils.hasCameraPermission(this)) {
                ActivityCompat.requestPermissions(this, new String[]{CAMERA_PERMISSION[0], WRITE_EXTERNAL_PERMISSION[0]}, CAMERA);
            }
        }

        if (!isAndroidVersionNew || PermissionUtils.hasCameraPermission(this) ||
                PermissionUtils.hasWritePermission(this)) {
            Intent takePhotoIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);

            imageUri = FileProvider.getUriForFile(this,
                    getApplicationContext().getPackageName() + ".my.package.name.provider",
                    FileUtils.createFileWithExtension("jpg"));

            takePhotoIntent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            takePhotoIntent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
            startActivityForResult(takePhotoIntent, CAMERA);
        }
    }

    private void voiceRecordingAction(){

        boolean isAndroidVersionNew = Build.VERSION.SDK_INT > Build.VERSION_CODES.LOLLIPOP_MR1;
        if (isAndroidVersionNew) {
            if (!PermissionUtils.hasAudioRecordPermission(this)) {
                ActivityCompat.requestPermissions(this, new String[]{WRITE_EXTERNAL_PERMISSION[0], RECORD_AUDIO_PERMISSION[0]}, RECORD_AUDIO);
            }
        }

        if (!isAndroidVersionNew || PermissionUtils.hasAudioRecordPermission(this)
                || PermissionUtils.hasWritePermission(this)) {

            if(!recording){
                Toast.makeText(ChatActivity.this, "녹음 시작", Toast.LENGTH_SHORT).show();
                messageRecordButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_stop_black_24px));

                initializeMediaRecord();
                startRecordingAudio();
            }
            else{
                Toast.makeText(ChatActivity.this, "녹음 종료", Toast.LENGTH_SHORT).show();

                messageRecordButton.setImageDrawable(getResources().getDrawable(R.drawable.ic_keyboard_voice_black_24px));
                stopRecordingAudio();
                sendRecord();
            }
            recording = !recording;
        }
    }

    private void startRecordingAudio(){
        File audioFile = FileUtils.createFileWithExtension("3gpp");
        recordUrl = null;
        recordPath = audioFile.getAbsolutePath();
        mediaRecorder.setOutputFile(recordPath);

        try {
            mediaRecorder.prepare();
            mediaRecorder.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void stopRecordingAudio(){

        if(mediaRecorder != null){
            mediaRecorder.stop();
            mediaRecorder.release();

            mediaRecorder = null;
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == this.RESULT_CANCELED) {
            return;
        }

        if (requestCode == GALLERY) {
            if (data != null) {
                Uri contentURI = data.getData();
                try {
                    Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), contentURI);
                    imagePath = saveImage(image);

                    sendImage(resizedImage);

                    Toast.makeText(ChatActivity.this, "저장되었습니다", Toast.LENGTH_SHORT).show();

                } catch (IOException e) {
                    e.printStackTrace();
                    Toast.makeText(ChatActivity.this, "Failed!", Toast.LENGTH_SHORT).show();
                }
            }

        } else if (requestCode == CAMERA) {

            try {
                Bitmap image = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                imagePath = saveImage(image);
                Toast.makeText(ChatActivity.this, "저장되었습니다", Toast.LENGTH_SHORT).show();

                sendImage(resizedImage);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String permissions[], @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case GALLERY: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    choosePhotoFromGallery();

                } else {
                    Toast.makeText(this, "GALLERY DENIED", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case CAMERA: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    takePhotoFromCamera();

                } else {
                    Toast.makeText(this, "CAMERA DENIED", Toast.LENGTH_LONG).show();
                }
                break;
            }

            case RECORD_AUDIO: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    voiceRecordingAction();
                } else {
                    Toast.makeText(this, "RECORD AUDIO DENIED", Toast.LENGTH_LONG).show();
                }
                break;
            }


        }
    }

    public String saveImage(Bitmap myBitmap) {

        File file = FileUtils.createFileWithExtension("jpg");
        resizedImage = ImageUtils.resizeImage(myBitmap);
        ByteArrayOutputStream bytes = ImageUtils.compressImage(resizedImage);

        try (FileOutputStream fo = new FileOutputStream(file)) {
            fo.write(bytes.toByteArray());

            MediaScannerConnection.scanFile(this,
                    new String[]{file.getPath()},
                    new String[]{"image/jpeg"}, null);

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Log.d("TAG", "File Saved::--->" + file.getAbsolutePath());

        return file.getAbsolutePath();
    }


    private String getConversationId(String partnerId) {
        String myId = DatabaseUtils.getCurrentUUID();

        if (myId.compareTo(partnerId) < 0) {
            return myId + "-" + partnerId;
        } else {
            return partnerId + "-" + myId;
        }
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);

        messageListView.setVisibility(View.VISIBLE);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        DatabaseUtils.getMessagesByConversationId(conversationId).removeEventListener(messageListener);
    }
}