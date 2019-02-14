package com.asus.launcher.search.frequentcontact;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;

import com.asus.launcher.R;
import com.asus.launcher.search.contact.ContactUtil;
import com.asus.launcher.search.contact.PhoneNumberInteraction;
import com.asus.quickfind.client.AnalyticUtilities;
import com.asus.quickfind.client.ClientSettings;
import com.asus.quickfind.util.AnalyticConstants.EventAction;
import com.asus.quickfind.util.AnalyticConstants.EventCategory;
import com.asus.quickfind.util.AnalyticConstants.ScreenName;
import com.asus.quickfind.util.LogUtilities;

import java.util.ArrayList;
import java.util.concurrent.Executors;


public class ContactActionDialog extends DialogFragment {
    //package level access for ContactItem
    static final String TAG = "ContactActionDialog";
    private static final String ARG_DISPLAY_NAME = "display_name";
    private static final String ARG_CONTACT_KEY = "contact_key";
    private static final String ARG_CONTACT_ID = "contact_id";
    private static final String ARG_HAS_NUMBER = "has_number";


    public static ContactActionDialog newInstance(ContactData contactInfo) {
        ContactActionDialog dialog = new ContactActionDialog();
        Bundle args = new Bundle();
        args.putString(ARG_DISPLAY_NAME, contactInfo.getDisplayName());
        args.putString(ARG_CONTACT_KEY, contactInfo.getContactKey());
        args.putInt(ARG_CONTACT_ID, contactInfo.getContactId());
        args.putBoolean(ARG_HAS_NUMBER, contactInfo.getHasPhoneNumber());
        dialog.setArguments(args);
        return dialog;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        final Activity activity = getActivity();
        final String displayName = args.getString(ARG_DISPLAY_NAME);
        final int contactId = args.getInt(ARG_CONTACT_ID);
        final String contactKey = args.getString(ARG_CONTACT_KEY);
        final boolean hasPhoneNumber = args.getBoolean(ARG_HAS_NUMBER);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity, ClientSettings
                .getDialogTheme());
        final Resources res = activity.getResources();
        final String DIAL_TEXT = res.getString(R.string.call_contact);
        final String SMS_TEXT = res.getString(R.string.send_text_message);
        final String VIEW_CONTACT_TEXT = res.getString(R.string.view_contact_info);
        final String eventCategory = EventCategory.FREQUENT_CONTACTS;
        final ArrayList<String> arrayDialogList = new ArrayList<>();
        if (hasPhoneNumber) {
            if (PhoneNumberInteraction.canHandlePhoneCallIntent(activity)) {
                arrayDialogList.add(DIAL_TEXT);
            }
            if (PhoneNumberInteraction.canHandleSMSIntent(activity)) {
                arrayDialogList.add(SMS_TEXT);
            }
        }
        arrayDialogList.add(VIEW_CONTACT_TEXT);
        builder.setTitle(displayName)
                .setItems(arrayDialogList.toArray(new CharSequence[arrayDialogList.size()]), new
                        DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int position) {

                                String action = arrayDialogList.get(position);
                                if (action.equals(DIAL_TEXT)) {
                                    if (PhoneNumberInteraction.canHandlePhoneCallIntent
                                            (activity) && hasPhoneNumber) {
                                        AnalyticUtilities.sendEvent(activity, eventCategory,
                                                EventAction.CLICK_PHONE_CALL, null, null);
                                        AnalyticUtilities.sendView(activity, ScreenName
                                                .CLICK_FREQUENT_CONTACTS_PHONE_CALL);
                                        PhoneNumberInteraction.executePhoneCallTask
                                                (activity, contactKey, Executors
                                                        .newSingleThreadExecutor());
                                    }

                                } else if (action.equals(SMS_TEXT)) {
                                    if (PhoneNumberInteraction.canHandleSMSIntent
                                            (activity) && hasPhoneNumber) {
                                        AnalyticUtilities.sendEvent(activity, eventCategory,
                                                EventAction.CLICK_SMS, null, null);
                                        AnalyticUtilities.sendView(activity, ScreenName
                                                .CLICK_FREQUENT_CONTACTS_SMS);
                                        PhoneNumberInteraction.executeSMSTask(activity,
                                                contactKey, Executors.newSingleThreadExecutor());
                                    }


                                } else if (action.equals(VIEW_CONTACT_TEXT)) {
                                    AnalyticUtilities.sendEvent(activity, eventCategory,
                                            EventAction.CLICK_CONTACT, null, null);
                                    AnalyticUtilities.sendView(activity, ScreenName
                                            .CLICK_FREQUENT_CONTACTS);
                                    ContactUtil.startViewContactDetailActivity(activity,
                                            contactId, contactKey);
                                } else {
                                    LogUtilities.e(TAG, "unknown Action !!");

                                }
                            }
                        });
        return builder.create();
    }

}


