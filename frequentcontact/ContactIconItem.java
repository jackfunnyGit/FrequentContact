package com.asus.launcher.search.frequentcontact;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import com.asus.launcher.R;
import com.asus.launcher.search.contact.ContactUtil;
import com.asus.launcher.search.contact.PhoneNumberInteraction;
import com.asus.quickfind.client.AnalyticUtilities;
import com.asus.quickfind.util.AnalyticConstants;
import com.asus.quickfind.util.LogUtilities;
import com.asus.quickfind.view.pager.IconPagerAdapter;

import java.util.ArrayList;
import java.util.concurrent.Executors;


public class ContactIconItem extends IconPagerAdapter.IconItem {
    private static final String LOG_TAG = "ContactIconItem";
    private static Drawable sDefaultPhoto;
    public final ContactData contactData;

    public ContactIconItem(@NonNull final ContactData contactData) {
        super(contactData.getContactKey(), contactData.getDisplayName());
        this.contactData = contactData;
    }

    @Override
    public Object getMetadata() {
        return contactData;
    }

    @Override
    public Drawable getDefaultIcon(@NonNull Context context) {
        return getCircularDefaultPhoto(context);
    }

    @Nullable
    private static ContactData getContactInfo(@NonNull final View view) {
        final Object tag = view.getTag();
        return tag instanceof ContactData ? (ContactData) tag : null;
    }

    private Drawable getCircularDefaultPhoto(@NonNull Context context) {
        if (sDefaultPhoto == null) {
            Resources res = context.getResources();
            Bitmap defaultPhoto = ContactUtil.getCircularDefaultPhoto(context);
            sDefaultPhoto = createBitmapDrawable(res, defaultPhoto);
        }
        return sDefaultPhoto;
    }

    public static void clearDefaultPhoto() {
        sDefaultPhoto = null;
    }

    public static class Listener implements IconPagerAdapter.IconListener {

        private static final boolean ACTION_STATE = true;
        private static final boolean VIEW_STATE = false;
        private static final boolean ORIENTATION_RIGHT = true;
        private static final boolean ORIENTATION_LEFT = false;

        private View mActionView;

        @Override
        public void onClick(View viewClick) {
            final Context context = viewClick.getContext();
            final ContactData contactInfo = getContactInfo(viewClick);
            if (contactInfo == null) {
                LogUtilities.e(LOG_TAG, "contactInfo is null!!!");
                return;
            }
            final ViewGroup parent = (ViewGroup) viewClick.getParent();
            ArrayList<View> childViews = UtilityAnimation.getChildViews(parent);
            if (childViews == null) {
                return;
            }
            //TODO get and bind actionView
            if (mActionView == null) {
                mActionView = getActionView(context, parent);
            }
            bindActionView(mActionView, contactInfo);
            //TODO implement Animation

            if (contactInfo.state == VIEW_STATE) {

                contactInfo.state = ACTION_STATE;
                UtilityAnimation.slideOut(childViews, viewClick, parent, mActionView);

            } else {
                contactInfo.state = VIEW_STATE;
                UtilityAnimation.slideIn(childViews, viewClick, parent, mActionView);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            ViewGroup viewGroup = (ViewGroup) view.getParent();
            LogUtilities.i(LOG_TAG, "ViewGroup is " + viewGroup);
            LogUtilities.i(LOG_TAG, "View is " + view);
            return true;
        }

        //TODO move it to a suitable position
        private View getActionView(@NonNull Context context, @NonNull ViewGroup parent) {
            return UtilityAnimation.getActionView(context, parent, R.layout.quick_find_action_view);
        }

        private void bindActionView(@NonNull View actionView, ContactData contactData) {
            final boolean hasPhoneNumber = contactData.getHasPhoneNumber();
            final String contactKey = contactData.getContactKey();
            final String eventCategory = AnalyticConstants.EventCategory.FREQUENT_CONTACTS;
            final Context context = actionView.getContext();
            ImageView smsImage = (ImageView) actionView.findViewById(R.id.contact_list_sms);
            smsImage.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View viewClick) {
                    if (PhoneNumberInteraction.canHandleSMSIntent
                            (context) && hasPhoneNumber) {
                        AnalyticUtilities.sendEvent(context, eventCategory,
                                AnalyticConstants.EventAction.CLICK_SMS, null, null);
                        AnalyticUtilities.sendView(context, AnalyticConstants.ScreenName
                                .CLICK_FREQUENT_CONTACTS_SMS);
                        PhoneNumberInteraction.executeSMSTask(context,
                                contactKey, Executors.newSingleThreadExecutor());
                    }
                }
            });
            ImageView dialImage = (ImageView) actionView.findViewById(R.id.contact_list_call);
            dialImage.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View viewClick) {
                    if (PhoneNumberInteraction.canHandlePhoneCallIntent
                            (context) && hasPhoneNumber) {
                        AnalyticUtilities.sendEvent(context, eventCategory,
                                AnalyticConstants.EventAction.CLICK_PHONE_CALL, null, null);
                        AnalyticUtilities.sendView(context, AnalyticConstants
                                .ScreenName
                                .CLICK_FREQUENT_CONTACTS_PHONE_CALL);
                        PhoneNumberInteraction.executePhoneCallTask
                                (context, contactKey, Executors
                                        .newSingleThreadExecutor());
                    }
                }
            });

        }

    }
}
