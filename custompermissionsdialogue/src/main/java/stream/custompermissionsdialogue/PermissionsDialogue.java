package stream.custompermissionsdialogue;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.provider.Settings;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

import stream.custombutton.CustomButton;
import stream.custompermissionsdialogue.ui.BounceInterpolator;
import stream.custompermissionsdialogue.ui.CustomBlurDialogue;
import stream.custompermissionsdialogue.utils.PermissionUtils;

public class PermissionsDialogue extends DialogFragment {

    public static Integer NOTREQUIRED = 0;
    public static Integer REQUIRED = 1;
    public static Integer OPTIONAL = 2;
    public static String REQUIRE_PHONE = "REQUIRE_PHONE";
    public static String REQUIRE_SMS = "REQUIRE_SMS";
    public static String REQUIRE_CONTACTS = "REQUIRE_CONTACTS";
    public static String REQUIRE_CALENDAR = "REQUIRE_CALENDAR";
    public static String REQUIRE_STORAGE = "REQUIRE_STORAGE";
    public static String REQUIRE_CAMERA = "REQUIRE_CAMERA";
    public static String REQUIRE_AUDIO = "REQUIRE_AUDIO";
    public static String REQUIRE_LOCATION = "REQUIRE_LOCATION";
    private static final int REQUEST_PERMISSIONS = 1;
    private static final int REQUEST_PERMISSION = 2;

    private Button mButton;
    private CustomPermissionsAdapter requiredAdapter;
    private CustomPermissionsAdapter optionalAdapter;
    private ArrayList<String> requiredPermissions;
    private ArrayList<String> optionalPermissions;

    private Context mContext;
    private Builder builder;
    private Integer gravity = Gravity.CENTER;
    private static PermissionsDialogue instance = new PermissionsDialogue();
    public static final String TAG = PermissionsDialogue.class.getSimpleName();

    public static PermissionsDialogue getInstance() {
        return instance;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {

        if (savedInstanceState != null) {
            if (builder == null) {
                builder = savedInstanceState.getParcelable(Builder.class.getSimpleName());
            }
        }
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.PermissionsDialogue);
        setRetainInstance(true);
        if (builder != null)
        {
            if (!builder.getCancelable())
            {
                this.setCancelable(false);
            }
            else
            {
                this.setCancelable(true);
            }
        }
        super.onCreate(savedInstanceState);
        mContext = getActivity();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (builder != null)
            outState.putParcelable(Builder.class.getSimpleName(), builder);
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Dialog dialog = super.onCreateDialog(savedInstanceState);
        //Configure floating window
        Window window = dialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = WindowManager.LayoutParams.MATCH_PARENT; //Floating window WRAPS_CONTENT by default. Force fullscreen
        wlp.height = WindowManager.LayoutParams.MATCH_PARENT;
        wlp.windowAnimations = R.style.CustomDialogAnimation;
        wlp.gravity = Gravity.CENTER;
        window.setAttributes(wlp);

        if (builder != null) {
            if (!builder.getCancelable()) {
                setCancelable(false);
            } else {
                setCancelable(true);
            }
        }

        return dialog;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.alert_permissions, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mButton = getView().findViewById(R.id.permissions_btn);

        initPermissionsView(view);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        Log.d("Request Code", String.valueOf(requestCode));
        switch (requestCode) {
            case REQUEST_PERMISSIONS: {
                Log.d("Permissons", "Request Permissions");
                refreshRequiredPermissions();
                boolean permissionsGranted = true;
                if (grantResults.length > 0) {
                    for (int i = 0; i < grantResults.length; i++) {
                        if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                            boolean showRationale = shouldShowRequestPermissionRationale(permissions[i]);
                            if (!showRationale) {
                                permissionsGranted = false;
                            }
                        }
                    }
                }

                if (permissionsGranted) {
                    refreshPermissionsButton(false);
                    Log.d("Permissions", "Granted");
                } else {
                    refreshPermissionsButton(true);
                    Log.d("Permissions", "Denied");
                }
                return;
            }

            case REQUEST_PERMISSION: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    refreshOptionalPermissions();
                } else {

                }
                return;
            }
        }
    }

    private void initPermissionsView(View view)
    {
        if (builder != null) {

            Log.d(TAG, "Builder Not Null");

            if (builder.getRequiredPermissions().size() != 0)
            {
                initPermissionsRecyclerView(view);
                initPermissionsButton(view);
            }
            else
            {
                LinearLayout permissionsLayout = view.findViewById(R.id.permissions_required);
                permissionsLayout.setVisibility(View.GONE);
            }

            if (builder.getOptionalPermissions().size() != 0)
            {
                initOptionalPermissionsRecyclerView(view);
            }
            else
            {
                LinearLayout permissionsLayout = view.findViewById(R.id.permissions_optional);
                permissionsLayout.setVisibility(View.GONE);
            }
        }
        else
        {
            Log.d(TAG, "Builder Null");
        }
    }

    private void initPermissionsRecyclerView(View view) {

        float radius = 5;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && builder.getDecorView() != null)
        {
            CustomBlurDialogue blurDialogue = view.findViewById(R.id.blurview);
            blurDialogue.create(builder.getDecorView(), radius);
        }

        if (builder.getTitle() != null)
        {
            TextView title = view.findViewById(R.id.title);
            title.setText(builder.getTitle());
        }
        if (builder.getMessage() != null)
        {
            TextView message = view.findViewById(R.id.message);
            message.setText(builder.getMessage());
        }
        else
        {
            TextView message = view.findViewById(R.id.message);
            message.setVisibility(View.GONE);
        }
        if (builder.getShowIcon() == false)
        {
            ImageView icon = view.findViewById(R.id.icon);
            icon.setVisibility(View.GONE);
        }
        else
        {
            if (builder.getIcon() != 0)
            {
                ImageView icon = view.findViewById(R.id.icon);
                icon.setImageDrawable(ContextCompat.getDrawable(mContext, builder.getIcon()));
            }
        }

        requiredPermissions = new ArrayList<>();
        requiredPermissions = builder.getRequiredPermissions();
        int spanSize = requiredPermissions.size();
        RecyclerView permissionsRecyclerView = view.findViewById(R.id.permissions_list);
        requiredAdapter = new CustomPermissionsAdapter(mContext, requiredPermissions, false);
        permissionsRecyclerView.setAdapter(requiredAdapter);
        GridLayoutManager layoutManager= new GridLayoutManager(mContext, spanSize, LinearLayoutManager.VERTICAL, false);
        permissionsRecyclerView.setLayoutManager(layoutManager);
    }

    private void initOptionalPermissionsRecyclerView(View view) {

        float radius = 5;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && builder.getDecorView() != null) {
            CustomBlurDialogue blurDialogueOptional = view.findViewById(R.id.blurview_optional);
            blurDialogueOptional.create(builder.getDecorView(), radius);
        }

        if (builder.getMessageOptional() != null)
        {
            TextView messageOptional = view.findViewById(R.id.permissions_optional_text);
            messageOptional.setText(builder.getMessageOptional());
        }
        optionalPermissions = new ArrayList<>();
        optionalPermissions = builder.getOptionalPermissions();
        int spanSize = optionalPermissions.size();
        if (spanSize > 2)
        {
            spanSize = 2;
        }
        RecyclerView permissionsRecyclerView = view.findViewById(R.id.permissions_list_optional);
        optionalAdapter = new CustomPermissionsAdapter(mContext, optionalPermissions, true);
        permissionsRecyclerView.setAdapter(optionalAdapter);
        GridLayoutManager layoutManager= new GridLayoutManager(mContext, spanSize, LinearLayoutManager.VERTICAL, false);
        permissionsRecyclerView.setLayoutManager(layoutManager);
        int spacing = Units.dpToPx(mContext, 40);
        boolean includeEdge = true;
        permissionsRecyclerView.addItemDecoration(new GridSpacingItemDecoration(spanSize, spacing, includeEdge));
    }

    private void initPermissionsButton(View view) {
        mButton = view.findViewById(R.id.permissions_btn);
        if (getRequiredRequestPermissions().size() == 0)
        {
            mButton.setText("Continue");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.icon_add_activated_selector));
            }
            else
            {
                mButton.setBackgroundDrawable((ContextCompat.getDrawable(mContext, R.drawable.icon_add_activated_selector)));
            }
            if (builder.getOnContinueClicked() != null)
            {
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        builder.getOnContinueClicked().OnClick(getView(), getDialog());
                    }
                });
            }
            else
            {
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mButton.startAnimation(AnimateButton());
                        Handler handler = new Handler();
                        Runnable r = new Runnable() {
                            public void run() {
                                dismiss();
                            }
                        };
                        handler.postDelayed(r, 250);
                    }
                });
            }
        }
        else
        {
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mButton.startAnimation(AnimateButton());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ArrayList<String> requestPermissions = getRequiredRequestPermissions();
                        if (!requestPermissions.isEmpty()) {
                            requestPermissions(requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSIONS);
                        }
                        else
                        {

                        }
                    } else {

                    }
                }
            });
        }
    }

    private void refreshRequiredPermissions()
    {
        requiredPermissions = builder.getRequiredPermissions();
        requiredAdapter.permissionsList = requiredPermissions;
        requiredAdapter.notifyDataSetChanged();
    }

    private void refreshOptionalPermissions()
    {
        optionalPermissions = builder.getOptionalPermissions();
        optionalAdapter.permissionsList = optionalPermissions;
        optionalAdapter.notifyDataSetChanged();
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    private void refreshPermissionsButton(boolean denied)
    {
        if (denied)
        {
            mButton.setText("DENIED - Open Settings");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.icon_add_error_selector));
            }
            else
            {
                mButton.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.icon_add_error_selector));
            }
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    dismiss();
                    Toast.makeText(mContext, "Click Permissions to enable permissions", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                            Uri.parse("package:" + getActivity().getPackageName()));
                    intent.addCategory(Intent.CATEGORY_DEFAULT);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
            });
        }
        else if (getRequiredRequestPermissions().size() == 0)
        {
            mButton.setText("Success!");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.icon_add_activated_selector));
            }
            else
            {
                mButton.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.icon_add_activated_selector));
            }
            if (builder.getOnContinueClicked() != null)
            {
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        builder.getOnContinueClicked().OnClick(getView(), getDialog());
                    }
                });
            }
            else
            {
                mButton.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        mButton.startAnimation(AnimateButton());
                        Handler handler = new Handler();
                        Runnable r = new Runnable() {
                            public void run() {
                                dismiss();
                            }
                        };
                        handler.postDelayed(r, 250);
                    }
                });
            }

            Handler handler = new Handler();
            Runnable r = new Runnable() {
                public void run() {
                    mButton.setText("Continue");
                }
            };
            handler.postDelayed(r, 1500);
        }
        else {
            mButton.setText("Permission Denied");
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                mButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.icon_add_error_selector));
            }
            else
            {
                mButton.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.icon_add_error_selector));
            }
            mButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mButton.startAnimation(AnimateButton());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                        ArrayList<String> requestPermissions = getRequiredRequestPermissions();
                        if (!requestPermissions.isEmpty()) {
                            requestPermissions(requestPermissions.toArray(new String[requestPermissions.size()]), REQUEST_PERMISSIONS);
                        } else {

                        }
                    } else {

                    }
                }
            });

            Handler handler = new Handler();
            Runnable r = new Runnable() {
                public void run() {
                    mButton.setText("Grant Permissions");
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                        mButton.setBackground(ContextCompat.getDrawable(mContext, R.drawable.icon_add));
                    }
                    else
                    {
                        mButton.setBackgroundDrawable(ContextCompat.getDrawable(mContext, R.drawable.icon_add));
                    }
                }
            };
            handler.postDelayed(r, 1500);
        }
        mButton.startAnimation(AnimateButton());
    }

    private Dialog show(Activity activity, Builder builder) {
        this.builder = builder;
        if (!isAdded())
            show(((AppCompatActivity) activity).getSupportFragmentManager(), TAG);
        return getDialog();
    }

    public static class Builder implements Parcelable {

        private String title;
        private String message;
        private String messageOptional;

        private OnContinueClicked onContinueClicked;

        private boolean autoHide;
        private boolean cancelable = true;
        private boolean showIcon = true;
        private int icon;
        private Integer phone = 0;
        private Integer sms = 0;
        private Integer contacts = 0;
        private Integer calendar = 0;
        private Integer storage = 0;
        private Integer camera = 0;
        private Integer audio = 0;
        private Integer location = 0;
        private String phonedescription;
        private String smsdescription;
        private String contactsdescription;
        private String calendardescription;
        private String storagedescription;
        private String cameradescription;
        private String audiodescription;
        private String locationdescription;

        private View decorView;

        private Context context;

        protected Builder(Parcel in) {
            title = in.readString();
            message = in.readString();
            messageOptional = in.readString();
            autoHide = in.readByte() != 0;
            cancelable = in.readByte() != 0;
            showIcon = in.readByte() != 0;
            icon = in.readInt();
            phone = in.readInt();
            sms = in.readInt();
            contacts = in.readInt();
            calendar = in.readInt();
            storage = in.readInt();
            camera = in.readInt();
            audio = in.readInt();
            location = in.readInt();
            phonedescription = in.readString();
            smsdescription = in.readString();
            contactsdescription = in.readString();
            calendardescription = in.readString();
            storagedescription = in.readString();
            cameradescription = in.readString();
            audiodescription = in.readString();
            locationdescription = in.readString();
        }

        public Builder getBuilder() { return this; }

        /**
         * @param title - set Required Permissions title text.
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }
        public String getTitle() { return title; }

        /**
         * @param message - set Required Permissions message text.
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }
        public String getMessage() {
            return message;
        }

        /**
         * @param messageOptional - set Optional Permissions message text.
         * @return
         */
        public Builder setMessageOptional(String messageOptional) {
            this.messageOptional = messageOptional;
            return this;
        }
        public String getMessageOptional() {
            return messageOptional;
        }

        /**
         * @param onContinueClicked - pass a listener to be called when the `Continue` button is clicked.
         * @return
         */
        public Builder setOnContinueClicked(OnContinueClicked onContinueClicked) {
            this.onContinueClicked = onContinueClicked;
            return this;
        }
        public OnContinueClicked getOnContinueClicked() {
            return onContinueClicked;
        }

        public Builder setAutoHide(boolean autoHide) {
            this.autoHide = autoHide;
            return this;
        }
        public boolean isAutoHide() {
            return autoHide;
        }

        /**
         * @param cancelable - set `false` to prevent dialogue dismissal without user granting required permissions.
         * @return
         */
        public Builder setCancelable(boolean cancelable) {
            this.cancelable = cancelable;
            return this;
        }
        public boolean getCancelable() { return cancelable; }

        /**
         * @param showicon - toggle icon visibility. Default icon shown is app icon.
         * @return
         */
        public Builder setShowIcon(boolean showicon) {
            this.showIcon = showicon;
            return this;
        }
        public boolean getShowIcon() { return showIcon; }

        /**
         * @param icon - pass a drawable resource Id to set as icon.
         * @return
         */
        public Builder setIcon(int icon) {
            this.icon = icon;
            return this;
        }
        public int getIcon() { return icon; }

        /**
         * @param phone - set REQUIRED or OPTIONAL flag to display permission request.
         * @return
         */
        public Builder setRequirePhone(Integer phone) {
            this.phone = phone;
            return this;
        }
        public Integer requirePhone() {
            return phone;
        }

        /**
         * @param sms - set REQUIRED or OPTIONAL flag to display permission request.
         * @return
         */
        public Builder setRequireSMS(Integer sms) {
            this.sms = sms;
            return this;
        }
        public Integer requireSMS() {
            return sms;
        }

        /**
         * @param contacts - set REQUIRED or OPTIONAL flag to display permission request.
         * @return
         */
        public Builder setRequireContacts(Integer contacts) {
            this.contacts = contacts;
            return this;
        }
        public Integer requireContacts() {
            return contacts;
        }

        /**
         * @param calendar - set REQUIRED or OPTIONAL flag to display permission request.
         * @return
         */
        public Builder setRequireCalendar(Integer calendar) {
            this.calendar = calendar;
            return this;
        }
        public Integer requireCalendar() {
            return calendar;
        }

        /**
         * @param storage - set REQUIRED or OPTIONAL flag to display permission request.
         * @return
         */
        public Builder setRequireStorage(Integer storage) {
            this.storage = storage;
            return this;
        }
        public Integer requireStorage() {
            return storage;
        }

        /**
         * @param camera - set REQUIRED or OPTIONAL flag to display permission request.
         * @return
         */
        public Builder setRequireCamera(Integer camera) {
            this.camera = camera;
            return this;
        }
        public Integer requireCamera() {
            return camera;
        }

        /**
         * @param audio - set REQUIRED or OPTIONAL flag to display permission request.
         * @return
         */
        public Builder setRequireAudio(Integer audio) {
            this.audio = audio;
            return this;
        }
        public Integer requireAudio() {
            return audio;
        }

        /**
         * @param location - set REQUIRED or OPTIONAL flag to display permission request.
         * @return
         */
        public Builder setRequireLocation(Integer location) {
            this.location = location;
            return this;
        }
        public Integer requireLocation() {
            return location;
        }

        /**
         * @param phonedescription - set optional phone permission text.
         * @return
         */
        public Builder setPhoneDescription(String phonedescription) {
            this.phonedescription = phonedescription;
            return this;
        }
        public String getPhoneDescription() {
            return phonedescription;
        }

        /**
         * @param smsdescription - set optional text message permission text.
         * @return
         */
        public Builder setSMSDescription(String smsdescription) {
            this.smsdescription = smsdescription;
            return this;
        }
        public String getSMSDescription() {
            return smsdescription;
        }

        /**
         * @param contactsdescription - set optional contact permission text.
         * @return
         */
        public Builder setContactDescription(String contactsdescription) {
            this.contactsdescription = contactsdescription;
            return this;
        }
        public String getContactDescription() {
            return contactsdescription;
        }

        /**
         * @param calendardescription - set optional calendar permission text.
         * @return
         */
        public Builder setCalendarDescription(String calendardescription) {
            this.calendardescription = calendardescription;
            return this;
        }
        public String getCalendarDescription() {
            return calendardescription;
        }

        /**
         * @param storagedescription - set optional storage permission text.
         * @return
         */
        public Builder setStorageDescription(String storagedescription) {
            this.storagedescription = storagedescription;
            return this;
        }
        public String getStorageDescription() {
            return storagedescription;
        }

        /**
         * @param cameradescription - set optional camera permission text.
         * @return
         */
        public Builder setCameraDescription(String cameradescription) {
            this.cameradescription = cameradescription;
            return this;
        }
        public String getCameraDescription() {
            return cameradescription;
        }

        /**
         * @param audiodescription - set optional audio permission text.
         * @return
         */
        public Builder setAudioDescription(String audiodescription) {
            this.audiodescription = audiodescription;
            return this;
        }
        public String getAudioDescription() {
            return audiodescription;
        }

        /**
         * @param locationdescription - set optional location permission text.
         * @return
         */
        public Builder setLocationDescription(String locationdescription) {
            this.locationdescription = locationdescription;
            return this;
        }
        public String getLocationDescription() {
            return locationdescription;
        }

        public ArrayList<String> getRequiredPermissions()
        {
            ArrayList<String> requiredPermissions = new ArrayList<>();
            if (requirePhone() == REQUIRED)
            {
                requiredPermissions.add(REQUIRE_PHONE);
            }
            if (requireSMS() == REQUIRED)
            {
                requiredPermissions.add(REQUIRE_SMS);
            }
            if (requireContacts() == REQUIRED)
            {
                requiredPermissions.add(REQUIRE_CONTACTS);
            }
            if (requireCalendar() == REQUIRED)
            {
                requiredPermissions.add(REQUIRE_CALENDAR);
            }
            if (requireStorage() == REQUIRED)
            {
                requiredPermissions.add(REQUIRE_STORAGE);
            }
            if (requireCamera() == REQUIRED)
            {
                requiredPermissions.add(REQUIRE_CAMERA);
            }
            if (requireAudio() == REQUIRED)
            {
                requiredPermissions.add(REQUIRE_AUDIO);
            }
            if (requireLocation() == REQUIRED)
            {
                requiredPermissions.add(REQUIRE_LOCATION);
            }
            return requiredPermissions;
        }

        public ArrayList<String> getOptionalPermissions()
        {
            ArrayList<String> requiredPermissions = new ArrayList<>();
            if (requirePhone() == OPTIONAL)
            {
                requiredPermissions.add(REQUIRE_PHONE);
            }
            if (requireSMS() == OPTIONAL)
            {
                requiredPermissions.add(REQUIRE_SMS);
            }
            if (requireContacts() == OPTIONAL)
            {
                requiredPermissions.add(REQUIRE_CONTACTS);
            }
            if (requireCalendar() == OPTIONAL)
            {
                requiredPermissions.add(REQUIRE_CALENDAR);
            }
            if (requireStorage() == OPTIONAL)
            {
                requiredPermissions.add(REQUIRE_STORAGE);
            }
            if (requireCamera() == OPTIONAL)
            {
                requiredPermissions.add(REQUIRE_CAMERA);
            }
            if (requireAudio() == OPTIONAL)
            {
                requiredPermissions.add(REQUIRE_AUDIO);
            }
            if (requireLocation() == OPTIONAL)
            {
                requiredPermissions.add(REQUIRE_LOCATION);
            }
            return requiredPermissions;
        }

        /**
         * @param decorView - pass the Window DecorView for a nice blurred background. Defaults to overlay color.
         *                  Here's how to pass the correct DecorView in the following classes:
         *                  Activity - use `getWindow().getDecorView()`
         *                  Fragment - use `getActivity().getWindow().getDecorView()`
         *                  Viewholder - use `((Activity) mContext).getWindow().getDecorView()`
         * @return
         */
        @TargetApi(Build.VERSION_CODES.LOLLIPOP)
        public Builder setDecorView(View decorView) {

            this.decorView = decorView;
            return this;
        }
        public View getDecorView() { return decorView; }

        public Builder(Context context) { this.context = context; }

        /**
         * Construct the Dialogue Builder.
         * @return
         */
        public Builder build() {
            return this;
        }

        /**
         * Display the Dialogue with Builder parameters.
         * @return
         */
        public Dialog show() {
            return PermissionsDialogue.getInstance().show(((Activity) context), this);
        }

        public static final Creator<Builder> CREATOR = new Creator<Builder>() {
            @Override
            public Builder createFromParcel(Parcel in) {
                return new Builder(in);
            }

            @Override
            public Builder[] newArray(int size) {
                return new Builder[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel parcel, int i) {
            parcel.writeString(title);
            parcel.writeString(message);
            parcel.writeString(messageOptional);
            parcel.writeInt((byte) (autoHide ? 1 : 0));
            parcel.writeInt((byte) (cancelable ? 1 : 0));
            parcel.writeInt((byte) (showIcon ? 1 : 0));
            parcel.writeInt(icon);
            parcel.writeInt(phone);
            parcel.writeInt(sms);
            parcel.writeInt(contacts);
            parcel.writeInt(calendar);
            parcel.writeInt(storage);
            parcel.writeInt(camera);
            parcel.writeInt(audio);
            parcel.writeInt(location);
            parcel.writeString(phonedescription);
            parcel.writeString(smsdescription);
            parcel.writeString(contactsdescription);
            parcel.writeString(calendardescription);
            parcel.writeString(storagedescription);
            parcel.writeString(cameradescription);
            parcel.writeString(audiodescription);
            parcel.writeString(locationdescription);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    public interface OnContinueClicked {
        void OnClick(View view, Dialog dialog);
    }

    public ArrayList<String> getRequiredRequestPermissions()
    {
        ArrayList<String> requestPermissions = new ArrayList<>();
        if (builder.requirePhone() == REQUIRED) {
            if (!PermissionUtils.IsPermissionEnabled(mContext, Manifest.permission.CALL_PHONE)) {
                requestPermissions.add(Manifest.permission.CALL_PHONE);
            }
        }
        if (builder.requireSMS() == REQUIRED) {
            if (!PermissionUtils.IsPermissionEnabled(mContext, Manifest.permission.SEND_SMS)) {
                requestPermissions.add(Manifest.permission.SEND_SMS);
            }
        }
        if (builder.requireContacts() == REQUIRED) {
            if (!PermissionUtils.IsPermissionEnabled(mContext, Manifest.permission.WRITE_CONTACTS)) {
                requestPermissions.add(Manifest.permission.WRITE_CONTACTS);
            }
        }
        if (builder.requireCalendar() == REQUIRED) {
            if (!PermissionUtils.IsPermissionEnabled(mContext, Manifest.permission.WRITE_CALENDAR)) {
                requestPermissions.add(Manifest.permission.WRITE_CALENDAR);
            }
        }
        if (builder.requireStorage() == REQUIRED) {
            if (!PermissionUtils.IsPermissionEnabled(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        if (builder.requireCamera() == REQUIRED) {
            if (!PermissionUtils.IsPermissionEnabled(mContext, Manifest.permission.CAMERA)) {
                requestPermissions.add(Manifest.permission.CAMERA);
            }
        }
        if (builder.requireAudio() == REQUIRED) {
            if (!PermissionUtils.IsPermissionEnabled(mContext, Manifest.permission.RECORD_AUDIO)) {
                requestPermissions.add(Manifest.permission.RECORD_AUDIO);
            }
        }
        if (builder.requireLocation() == REQUIRED) {
            if (!PermissionUtils.IsPermissionEnabled(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
        return requestPermissions;
    }

    public ArrayList<String> getAllRequestPermissions()
    {
        ArrayList<String> requestPermissions = new ArrayList<>();
        if (builder.requirePhone() > NOTREQUIRED) {
            if (!PermissionUtils.IsPermissionEnabled(mContext, Manifest.permission.CALL_PHONE)) {
                requestPermissions.add(Manifest.permission.CALL_PHONE);
            }
        }
        if (builder.requireSMS() > NOTREQUIRED) {
            if (!PermissionUtils.IsPermissionEnabled(mContext, Manifest.permission.SEND_SMS)) {
                requestPermissions.add(Manifest.permission.SEND_SMS);
            }
        }
        if (builder.requireContacts() > NOTREQUIRED) {
            if (!PermissionUtils.IsPermissionEnabled(mContext, Manifest.permission.WRITE_CONTACTS)) {
                requestPermissions.add(Manifest.permission.WRITE_CONTACTS);
            }
        }
        if (builder.requireCalendar() > NOTREQUIRED) {
            if (!PermissionUtils.IsPermissionEnabled(mContext, Manifest.permission.WRITE_CALENDAR)) {
                requestPermissions.add(Manifest.permission.WRITE_CALENDAR);
            }
        }
        if (builder.requireStorage() > NOTREQUIRED) {
            if (!PermissionUtils.IsPermissionEnabled(mContext, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions.add(Manifest.permission.WRITE_EXTERNAL_STORAGE);
            }
        }
        if (builder.requireCamera() > NOTREQUIRED) {
            if (!PermissionUtils.IsPermissionEnabled(mContext, Manifest.permission.CAMERA)) {
                requestPermissions.add(Manifest.permission.CAMERA);
            }
        }
        if (builder.requireAudio() > NOTREQUIRED) {
            if (!PermissionUtils.IsPermissionEnabled(mContext, Manifest.permission.RECORD_AUDIO)) {
                requestPermissions.add(Manifest.permission.RECORD_AUDIO);
            }
        }
        if (builder.requireLocation() > NOTREQUIRED) {
            if (!PermissionUtils.IsPermissionEnabled(mContext, Manifest.permission.ACCESS_FINE_LOCATION)) {
                requestPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
            }
        }
        return requestPermissions;
    }

    public Animation AnimateButton() {
        // Load the animation
        final Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.bounce);
        double animationDuration = 1250;
        animation.setDuration((long) animationDuration);

        // Use custom animation interpolator to achieve the bounce effect
        BounceInterpolator interpolator = new BounceInterpolator(0.2, 20);
        animation.setInterpolator(interpolator);

        return animation;
    }

    public class CustomPermissionsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

        public Context mContext;
        public ArrayList<String> permissionsList;
        public boolean optional;

        public CustomPermissionsAdapter(Context context, ArrayList<String> permissionsList, boolean optional) {
            this.mContext = context;
            this.permissionsList = permissionsList;
            this.optional = optional;
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView;
            itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_permission, parent, false);
            return new PermissionViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder genericHolder, final int position) {
            String permission = permissionsList.get(position);
            PermissionViewHolder holder = (PermissionViewHolder) genericHolder;
            holder.setPermission(permission, optional);
        }

        @Override
        public int getItemCount() { return permissionsList.size(); }

        public String getPermission(int position) { return permissionsList.get(position); }
    }

    public class PermissionViewHolder extends RecyclerView.ViewHolder {

        public String permission;
        public TextView mName;
        public TextView mMessage;
        public ImageView mImage;
        public CustomButton mButton;
        public boolean optional;
        public Context mContext;

        public PermissionViewHolder(View itemView) {
            super(itemView);

            mName = itemView.findViewById(R.id.permission_name);
            mMessage = itemView.findViewById(R.id.permission_detail);
            mImage = itemView.findViewById(R.id.permission_icon);
            mButton = itemView.findViewById(R.id.permission_btn);
            mContext = itemView.getContext();
        }

        public void setPermission(String permission, boolean optional) {
            this.permission = permission;
            this.optional = optional;

            if (!optional)
                mButton.setVisibility(View.GONE);

            if (REQUIRE_PHONE.equals(permission))
            {
                mName.setText("Phone");
                mImage.setImageResource(R.drawable.ic_phone);
                setRequestPermissions(Manifest.permission.CALL_PHONE);

                if (builder.getPhoneDescription() != null)
                {
                    mMessage.setText(builder.getPhoneDescription());
                    mMessage.setVisibility(View.VISIBLE);
                }
                else
                {
                    mMessage.setVisibility(View.GONE);
                }
            }
            else if (REQUIRE_SMS.equals(permission))
            {
                mName.setText("SMS");
                mImage.setImageResource(R.drawable.ic_text);

                setRequestPermissions(Manifest.permission.SEND_SMS);

                if (builder.getSMSDescription() != null)
                {
                    mMessage.setText(builder.getSMSDescription());
                    mMessage.setVisibility(View.VISIBLE);
                }
                else
                {
                    mMessage.setVisibility(View.GONE);
                }
            }
            else if (REQUIRE_CONTACTS.equals(permission))
            {
                mName.setText("Contacts");

                mImage.setImageResource(R.drawable.ic_contacts);

                setRequestPermissions(Manifest.permission.WRITE_CONTACTS);

                if (builder.getContactDescription() != null)
                {
                    mMessage.setText(builder.getContactDescription());
                    mMessage.setVisibility(View.VISIBLE);
                }
                else
                {
                    mMessage.setVisibility(View.GONE);
                }
            }
            else if (REQUIRE_CALENDAR.equals(permission))
            {
                mName.setText("Calendar");
                mImage.setImageResource(R.drawable.ic_calendar);

                setRequestPermissions(Manifest.permission.WRITE_CALENDAR);

                if (builder.getCalendarDescription() != null)
                {
                    mMessage.setText(builder.getCalendarDescription());
                    mMessage.setVisibility(View.VISIBLE);
                }
                else
                {
                    mMessage.setVisibility(View.GONE);
                }
            }
            else if (REQUIRE_STORAGE.equals(permission))
            {
                mName.setText("Storage");
                mImage.setImageResource(R.drawable.ic_folder);

                setRequestPermissions(Manifest.permission.WRITE_EXTERNAL_STORAGE);

                if (builder.getStorageDescription() != null)
                {
                    mMessage.setText(builder.getStorageDescription());
                    mMessage.setVisibility(View.VISIBLE);
                }
                else
                {
                    mMessage.setVisibility(View.GONE);
                }
            }
            else if (REQUIRE_CAMERA.equals(permission))
            {
                mName.setText("Camera");
                mImage.setImageResource(R.drawable.ic_camera);

                setRequestPermissions(Manifest.permission.CAMERA);

                if (builder.getCameraDescription() != null)
                {
                    mMessage.setText(builder.getCameraDescription());
                    mMessage.setVisibility(View.VISIBLE);
                }
                else
                {
                    mMessage.setVisibility(View.GONE);
                }
            }
            else if (REQUIRE_AUDIO.equals(permission))
            {
                mName.setText("Audio");
                mImage.setImageResource(R.drawable.ic_mic);

                setRequestPermissions(Manifest.permission.RECORD_AUDIO);

                if (builder.getAudioDescription() != null)
                {
                    mMessage.setText(builder.getAudioDescription());
                    mMessage.setVisibility(View.VISIBLE);
                }
                else
                {
                    mMessage.setVisibility(View.GONE);
                }
            }
            else if (REQUIRE_LOCATION.equals(permission))
            {
                mName.setText("Location");
                mImage.setImageResource(R.drawable.ic_location);

                setRequestPermissions(Manifest.permission.ACCESS_FINE_LOCATION);

                if (builder.getLocationDescription() != null)
                {
                    mMessage.setText(builder.getLocationDescription());
                    mMessage.setVisibility(View.VISIBLE);
                }
                else
                {
                    mMessage.setVisibility(View.GONE);
                }
            }
            else
            {

            }
        }

        public void setRequestPermissions(final String requestPermission)
        {
            if (PermissionUtils.IsPermissionEnabled(mContext, requestPermission))
            {
                int color = ContextCompat.getColor(mContext, R.color.button_pressed);
                mImage.setColorFilter(color);
                if (optional) {
                    mButton.setText("Active");
                    mButton.setColor(ContextCompat.getColor(mContext, R.color.white), ContextCompat.getColor(mContext, R.color.white),
                            ContextCompat.getColor(mContext, R.color.green), ContextCompat.getColor(mContext, R.color.green_light),
                            ContextCompat.getColor(mContext, R.color.green), ContextCompat.getColor(mContext, R.color.green_light));
                    mButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            mButton.setPressStatus(true);
                        }
                    });
                }
            }
            else
            {
                int color = ContextCompat.getColor(mContext, R.color.button_inactive);
                mImage.setColorFilter(color);
                if (optional)
                {
                    mButton.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                                requestPermissions(new String[]{requestPermission}, REQUEST_PERMISSION);
                            }
                        }
                    });
                }
            }
        }
    }

    public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {

        private int spanCount;
        private int spacing;
        private boolean includeEdge;

        public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
            this.spanCount = spanCount;
            this.spacing = spacing;
            this.includeEdge = includeEdge;
        }

        @Override
        public void getItemOffsets(Rect outRect, View view, RecyclerView parent, RecyclerView.State state) {
            int position = parent.getChildAdapterPosition(view); // item position
            int column = position % spanCount; // item column

            if (includeEdge) {
                outRect.left = spacing - column * spacing / spanCount; // spacing - column * ((1f / spanCount) * spacing)
                outRect.right = (column + 1) * spacing / spanCount; // (column + 1) * ((1f / spanCount) * spacing)

//                if (position < spanCount) { // top edge
//                    outRect.top = spacing/2;
//                }
                outRect.bottom = spacing/2; // item bottom
            } else {
                outRect.left = column * spacing / spanCount; // column * ((1f / spanCount) * spacing)
                outRect.right = spacing - (column + 1) * spacing / spanCount; // spacing - (column + 1) * ((1f /    spanCount) * spacing)
                if (position >= spanCount) {
                    outRect.top = spacing; // item top
                }
            }
        }
    }

    public static class Units {
        /**
         * Converts dp to pixels.
         */
        public static int dpToPx(Context context, int dp) {
            DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
            int px = Math.round(dp * (displayMetrics.xdpi / DisplayMetrics.DENSITY_DEFAULT));
            return px;
        }
    }
}
