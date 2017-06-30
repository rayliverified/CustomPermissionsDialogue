package stream.sample;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;

import stream.custompermissionsdialogue.PermissionsDialogue;

public class PermissionsFragment extends Fragment {

    private PermissionsDialogue.Builder alertPermissions;

    FrameLayout mFrameLayout;
    TextView mTextLoading;

    Context mContext;

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
        mTextLoading = (TextView) rootView.findViewById(R.id.intro_text_loading);

        // Inflate the layout for this fragment
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        alertPermissions = new PermissionsDialogue.Builder(getActivity())
                .setMessage("Custom Permissions Dialogue is a sample permissions app and requires the Following permissions: ")
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
                .build();
        alertPermissions.show();
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
        if (alertPermissions.getBuilder() != null)
        {
            alertPermissions.show();
        }
    }

    @Override
    public void onAttach(Context context) { super.onAttach(context); }

    @Override
    public void onDetach() {
        super.onDetach();
    }
}