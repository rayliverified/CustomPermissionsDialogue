package stream.custompermissionsapp;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import stream.custombutton.CustomButton;
import stream.custompermissionsdialogue.PermissionsDialogue;

public class PermissionsFragment extends Fragment implements View.OnClickListener {

    private PermissionsDialogue.Builder alertPermissions;

    FrameLayout mFrameLayout;
    ImageView mBackground;
    CardView mCardView;
    CustomButton mButton1;
    CustomButton mButton2;
    CustomButton mButton3;
    CustomButton mButton4;
    CustomButton mButton5;
    CustomButton mButton6;

    Context mContext;
    public static final String mActivity = "PermissionsFragment";

    public PermissionsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mContext = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_permissions, container, false);
        mFrameLayout = rootView.findViewById(R.id.intro_layout);
        mBackground = rootView.findViewById(R.id.background);
        mCardView = rootView.findViewById(R.id.cardview);
        mButton1 = rootView.findViewById(R.id.btn_1);
        mButton2 = rootView.findViewById(R.id.btn_2);
        mButton3 = rootView.findViewById(R.id.btn_3);
        mButton4 = rootView.findViewById(R.id.btn_4);
        mButton5 = rootView.findViewById(R.id.btn_5);
        mButton6 = rootView.findViewById(R.id.btn_6);
        mButton1.setOnClickListener(this);
        mButton2.setOnClickListener(this);
        mButton3.setOnClickListener(this);
        mButton4.setOnClickListener(this);
        mButton5.setOnClickListener(this);
        mButton6.setOnClickListener(this);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onClick(View view) {

        switch (view.getId())
        {
            case R.id.btn_1:
                //All - Showcases all CustomPermissionsDialogue permissions.
                alertPermissions = new PermissionsDialogue.Builder(getActivity())
                        .setMessage(getString(R.string.app_name) + " is a sample permissions app and requires the following permissions: ")
                        .setIcon(R.mipmap.ic_launcher)
                        .setRequirePhone(PermissionsDialogue.REQUIRED)
                        .setRequireSMS(PermissionsDialogue.REQUIRED)
                        .setRequireContacts(PermissionsDialogue.REQUIRED)
                        .setRequireStorage(PermissionsDialogue.REQUIRED)
                        .setRequireCamera(PermissionsDialogue.OPTIONAL)
                        .setRequireAudio(PermissionsDialogue.OPTIONAL)
                        .setRequireCalendar(PermissionsDialogue.OPTIONAL)
                        .setRequireLocation(PermissionsDialogue.OPTIONAL)
                        .setOnContinueClicked(new PermissionsDialogue.OnContinueClicked() {
                            @Override
                            public void OnClick(View view, Dialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .setDecorView(((Activity) mContext).getWindow().getDecorView())
                        .build();
                alertPermissions.show();
                break;
            case R.id.btn_2:
                //Required - Displays all required permissions for the user to grant.
                alertPermissions = new PermissionsDialogue.Builder(getActivity())
                        .setMessage(getString(R.string.app_name) + " is a sample permissions app and requires the following permissions: ")
                        .setIcon(R.mipmap.ic_launcher)
                        .setRequirePhone(PermissionsDialogue.REQUIRED)
                        .setRequireSMS(PermissionsDialogue.REQUIRED)
                        .setRequireContacts(PermissionsDialogue.REQUIRED)
                        .setRequireStorage(PermissionsDialogue.REQUIRED)
                        .setOnContinueClicked(new PermissionsDialogue.OnContinueClicked() {
                            @Override
                            public void OnClick(View view, Dialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .setDecorView(((Activity) mContext).getWindow().getDecorView())
                        .build();
                alertPermissions.show();
                break;
            case R.id.btn_3:
                //Optional - Optional permissions allows user to selectively enable permissions.
                alertPermissions = new PermissionsDialogue.Builder(getActivity())
                        .setRequireCamera(PermissionsDialogue.OPTIONAL)
                        .setRequireAudio(PermissionsDialogue.OPTIONAL)
                        .setRequireCalendar(PermissionsDialogue.OPTIONAL)
                        .setRequireLocation(PermissionsDialogue.OPTIONAL)
                        .setCameraDescription("Capture images")
                        .setAudioDescription("Record audio messages")
                        .setCalendarDescription("Add notes to calendar")
                        .setLocationDescription("Geotag captured images")
                        .setOnContinueClicked(new PermissionsDialogue.OnContinueClicked() {
                            @Override
                            public void OnClick(View view, Dialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .setDecorView(((Activity) mContext).getWindow().getDecorView())
                        .build();
                alertPermissions.show();
                break;
            case R.id.btn_4:
                //Single - Requests a single permission from the user.
                alertPermissions = new PermissionsDialogue.Builder(getActivity())
                        .setMessage(getString(R.string.app_name) + " is a sample permissions app and requires the following permissions: ")
                        .setShowIcon(false)
                        .setRequireStorage(PermissionsDialogue.REQUIRED)
                        .setOnContinueClicked(new PermissionsDialogue.OnContinueClicked() {
                            @Override
                            public void OnClick(View view, Dialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .setDecorView(((Activity) mContext).getWindow().getDecorView())
                        .build();
                alertPermissions.show();
                break;
            case R.id.btn_5:
                //Combined - A single permission request combined with other optional permissions that the user can grant.
                alertPermissions = new PermissionsDialogue.Builder(getActivity())
                        .setMessage(getString(R.string.app_name) + " is a sample permissions app and requires the following permissions: ")
                        .setShowIcon(false)
                        .setRequireStorage(PermissionsDialogue.REQUIRED)
                        .setRequireCamera(PermissionsDialogue.OPTIONAL)
                        .setRequireAudio(PermissionsDialogue.OPTIONAL)
                        .setRequireCalendar(PermissionsDialogue.OPTIONAL)
                        .setRequireLocation(PermissionsDialogue.OPTIONAL)
                        .setOnContinueClicked(new PermissionsDialogue.OnContinueClicked() {
                            @Override
                            public void OnClick(View view, Dialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .setDecorView(((Activity) mContext).getWindow().getDecorView())
                        .build();
                alertPermissions.show();
                break;
            case R.id.btn_6:
                //Uncancelable - Set Cancelable to false to force user to grant permissions before proceeding.
                alertPermissions = new PermissionsDialogue.Builder(getActivity())
                        .setMessage(getString(R.string.app_name) + " is a sample permissions app and requires the following permissions: ")
                        .setShowIcon(false)
                        .setCancelable(false)
                        .setRequireStorage(PermissionsDialogue.REQUIRED)
                        .setOnContinueClicked(new PermissionsDialogue.OnContinueClicked() {
                            @Override
                            public void OnClick(View view, Dialog dialog) {
                                dialog.dismiss();
                            }
                        })
                        .setDecorView(((Activity) mContext).getWindow().getDecorView())
                        .build();
                alertPermissions.show();
                break;
            default:
                break;
        }
    }

    //Hide background for screenshots.
    public void HideBackground(boolean hide)
    {
        if (hide)
        {
            mBackground.setVisibility(View.GONE);
            mCardView.setVisibility(View.GONE);
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    HideBackground(false);
                }
            }, 5000);
        }
        else
        {
            mBackground.setVisibility(View.VISIBLE);
            mCardView.setVisibility(View.VISIBLE);
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onAttach(Context context) { super.onAttach(context); }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}