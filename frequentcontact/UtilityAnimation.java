package com.asus.launcher.search.frequentcontact;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.RelativeLayout;
import android.widget.TableRow;

import com.asus.launcher.R;
import com.asus.quickfind.util.LogUtilities;

import java.util.ArrayList;

public class UtilityAnimation {
    private static final String LOG_TAG = "UtilityAnimation";
    private static final int DURATION = 200;
    private static final float SHIFT = 500;
    private static final boolean ACTION_STATE = true;
    private static final boolean VIEW_STATE = false;
    private static final boolean ORIENTATION_RIGHT = true;
    private static final boolean ORIENTATION_LEFT = false;

    public interface AfterAnimationListener {
        void afterAnimationCallback();
    }

    public static View getActionView(@NonNull Context context, @NonNull ViewGroup parent,
                                     int layoutResource) {
        RelativeLayout actionView = (RelativeLayout) LayoutInflater.from(context).
                inflate(layoutResource, parent, false);

        final TableRow.LayoutParams tableLayoutChildParams = new TableRow.LayoutParams(0,
                TableRow.LayoutParams.MATCH_PARENT);

        //TODO change to columnNumber
        tableLayoutChildParams.weight = context.getResources().getInteger(R.integer
                .quick_find_frequent_contact_page_column_number) - 3;

        actionView.setLayoutParams(tableLayoutChildParams);
        return actionView;
    }

    public static View bindActionView(View actionView) {

        return actionView;
    }

    public static ArrayList<View> getChildViews(@NonNull final ViewGroup mainView) {
        //TODO extract this checking-block in the future
        Object object = mainView.getTag();
        if (!(object instanceof ArrayList)) {
            LogUtilities.e(LOG_TAG, "ViewTag is not ArrayList!!!");
            return null;
        }
        //TODO extract Views from ViewTag to childViews,is there any other way to achieve the
        // same goal (cast directly to ArrayList<View>)?
        final ArrayList objectViews = (ArrayList) object;
        final int arraySize = objectViews.size();
        final ArrayList<View> childViews = new ArrayList<>(arraySize);
        for (int index = 0; index < arraySize; index++) {
            Object viewObject = objectViews.get(index);
            if (!(viewObject instanceof View)) {
                LogUtilities.e(LOG_TAG, "Array contains not Views");
                return null;
            }
            childViews.add((View) viewObject);
        }
        return childViews;

    }

    public static void slideOut(@NonNull final ArrayList<View> childViews,
                                @NonNull final View viewClick,
                                @NonNull final ViewGroup parent, @NonNull final View actionView) {
        final int arraySize = childViews.size();
        if (arraySize < 0) {
            return;
        }
        float firstPosition = childViews.get(0).getX();
        for (int index = 0; index < arraySize; index++) {
            final View childView = childViews.get(index);
            //TODO change contactData to ViewHolder in the future
            final Object tag = childView.getTag();
            ContactData contactData = tag instanceof ContactData ? (ContactData) tag : null;
            contactData.position = childView.getX();
            if (!childView.equals(viewClick)) {
                LogUtilities.i(LOG_TAG, "viewOut != viewClick ");
                if (childView.getX() < viewClick.getX()) {
                    contactData.backOrientation = ORIENTATION_RIGHT;
                    UtilityAnimation.slideToLeft(childView);
                } else {
                    contactData.backOrientation = ORIENTATION_LEFT;
                    UtilityAnimation.slideToRight(childView);
                }
            } else {
                //TODO change firstPosition
                UtilityAnimation.moveToPosition(viewClick, firstPosition, new
                        UtilityAnimation.AfterAnimationListener() {
                            @Override
                            public void afterAnimationCallback() {
                                LogUtilities.i(LOG_TAG, "moveToPosition " +
                                        "afterCallback ... ");
                                parent.addView(actionView);
                            }
                        });
            }
        }
    }
    public static void slideIn(@NonNull final ArrayList<View> childViews,
                                @NonNull final View viewClick,
                                @NonNull final ViewGroup parent, @NonNull final View actionView){
        final int arraySize = childViews.size();
        if (arraySize < 0) {
            return;
        }
        for (int index = 0; index < arraySize; index++) {
            final View childView = childViews.get(index);
            //view.setVisibility(View.VISIBLE);==>view should be be visible after
            // AnimationEnd otherwise cause screen flashing
            //TODO change contactData to ViewHolder in the future
            final Object tag = childView.getTag();
            ContactData contactData = tag instanceof ContactData ? (ContactData) tag : null;
            if (viewClick.equals(childView)) {
                //TODO change to callback?
                parent.removeView(actionView);
                UtilityAnimation.moveToPosition(viewClick, contactData.position, new
                        UtilityAnimation.AfterAnimationListener() {
                            @Override
                            public void afterAnimationCallback() {
                                LogUtilities.i(LOG_TAG, "ACTION_STATE ... " +
                                        "callback");
                            }
                        });
                continue;
            }
            if (contactData.backOrientation) {
                UtilityAnimation.slideFromLeft(childView);
            } else {
                UtilityAnimation.slideFromRight(childView);
            }
        }
    }

    public static void setAnimate(@NonNull final View view, final View actionView) {
        ViewGroup parent = (ViewGroup) view.getParent();
        ArrayList<View> childViews = getChildViews(parent);
        if (childViews == null) {
            return;
        }


    }

    public static void setOnclickAnimate(@NonNull final ViewGroup mainView, final View actionView) {

        Object object = mainView.getTag();
        if (!(object instanceof ArrayList)) {
            LogUtilities.e(LOG_TAG, "ViewTag is not ArrayList!!!");
            return;
        }
        //TODO extract Views from ViewTag to childViews,is there any other way to achieve the
        // same goal (cast directly to ArrayList<View>)?
        final ArrayList objectViews = (ArrayList) object;
        final int arraySize = objectViews.size();
        final ArrayList<View> childViews = new ArrayList<>(arraySize);
        for (int index = 0; index < arraySize; index++) {
            Object viewObject = objectViews.get(index);
            if (!(viewObject instanceof View)) {
                LogUtilities.e(LOG_TAG, "Array contains not Views");
                return;
            }
            childViews.add((View) viewObject);
        }
        for (int childIndex = 0; childIndex < childViews.size(); childIndex++) {
            final View childView = childViews.get(childIndex);
            ViewHolder vh = new ViewHolder();
            childView.setTag(vh);
            childView.setOnClickListener(new View.OnClickListener() {
                public void onClick(final View viewClick) {
                    //TODO delete in the future
                    LogUtilities.e(LOG_TAG, "indexChild is " + mainView.indexOfChild(viewClick));
                    LogUtilities.e(LOG_TAG, "child count is  " + mainView.getChildCount());
                    LogUtilities.e(LOG_TAG, "main view  is  " + mainView);
                    LogUtilities.e(LOG_TAG, "click view is  " + viewClick);
                    //TODO implement onclick
                    ViewHolder vh = (ViewHolder) viewClick.getTag();
                    if (vh.state == VIEW_STATE) {

                        //TODO delete in the future
                        LogUtilities.i(LOG_TAG, "VIEW_STATE : ");
                        //jack--
                        vh.state = ACTION_STATE;

                        //TODO delete in the future
                        LogUtilities.i(LOG_TAG, "onClick " + " size =" +
                                " " +
                                childViews.size() + " mainView width " + mainView.getWidth());
                        for (int index = 0; index < childViews.size(); index++) {
                            LogUtilities.i(LOG_TAG, "x" + (index + 1) + " " + childViews.get
                                    (index).getX());
                        }
                        //jack--
                        float firstPosition = childViews.get(0).getX();
                        for (int index = 0; index < arraySize; index++) {
                            final View viewOut = childViews.get(index);
                            ViewHolder viewHolder = (ViewHolder) viewOut.getTag();
                            viewHolder.position = viewOut.getX();
                            if (!viewOut.equals(viewClick)) {
                                LogUtilities.i(LOG_TAG, "viewOut != viewClick ");
                                if (viewOut.getX() < viewClick.getX()) {
                                    viewHolder.backOrientation = ORIENTATION_RIGHT;
                                    UtilityAnimation.slideToLeft(viewOut);
                                } else {
                                    viewHolder.backOrientation = ORIENTATION_LEFT;
                                    UtilityAnimation.slideToRight(viewOut);
                                }

                            } else {
                                LogUtilities.i(LOG_TAG, "firstpoition x " + firstPosition + " " +
                                        "viewClick.getTranslationX() = " + viewClick
                                        .getTranslationX() +
                                        " viewTargetgetx =" + viewClick.getX());
                                //TODO change firstPosition
                                UtilityAnimation.moveToPosition(viewClick, firstPosition, new
                                        UtilityAnimation.AfterAnimationListener() {
                                            @Override
                                            public void afterAnimationCallback() {
                                                LogUtilities.i(LOG_TAG, "moveToPosition " +
                                                        "afterCallback ... ");
                                                //mainView.addView(actionView);
                                                if (mainView.indexOfChild(actionView) < 0) {
                                                    actionView.setVisibility(View.GONE);
                                                    mainView.addView(actionView);
                                                }
                                                actionView.setVisibility(View.VISIBLE);
                                                LogUtilities.i(LOG_TAG, "index = " + mainView
                                                        .indexOfChild(actionView));
                                            }
                                        });
                            }
                        }
                    } else {
                        LogUtilities.i(LOG_TAG, "ACTION_STATE ");
                        vh.state = VIEW_STATE;
                        for (int index = 0; index < arraySize; index++) {
                            final View view = childViews.get(index);
                            //view.setVisibility(View.VISIBLE);==>view should be be visible after
                            // AnimationEnd otherwise cause screen flashing
                            ViewHolder viewHolder = (ViewHolder) view.getTag();
                            if (viewClick.equals(view)) {
                                //TODO change to callback?
                                //mainView.removeView(actionView);
                                actionView.setVisibility(View.GONE);
                                UtilityAnimation.moveToPosition(view, viewHolder.position, new
                                        UtilityAnimation.AfterAnimationListener() {
                                            @Override
                                            public void afterAnimationCallback() {
                                                LogUtilities.i(LOG_TAG, "ACTION_STATE ... " +
                                                        "callback");
                                            }
                                        });
                                continue;
                            }
                            if (viewHolder.backOrientation) {
                                UtilityAnimation.slideFromLeft(view);
                            } else {
                                UtilityAnimation.slideFromRight(view);
                            }


                        }


                    }
                }

            });

            childView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    //TODO implement onLongClick
                    LogUtilities.i(LOG_TAG, "index = " + mainView.indexOfChild(v));
                    return true;
                }
            });

        }//end for all views


    }

    // To animate view slide out from left to right
    public static void slideToRight(final View view) {
        TranslateAnimation animate = new TranslateAnimation(0, SHIFT, 0, 0);
        animate.setDuration(DURATION);
        animate.setFillAfter(true);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                view.setVisibility(View.GONE);
                view.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // nothing to do
            }

            @Override
            public void onAnimationStart(Animation animation) {
                // nothing to do
                view.setEnabled(false);
            }
        });
        view.startAnimation(animate);


    }

    // To animate view slide out from right to left
    public static void slideToLeft(final View view) {
        TranslateAnimation animate = new TranslateAnimation(0, -SHIFT, 0, 0);
        animate.setDuration(DURATION);
        animate.setFillAfter(true);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                view.setVisibility(View.GONE);
                view.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // nothing to do
            }

            @Override
            public void onAnimationStart(Animation animation) {
                // nothing to do
                view.setEnabled(false);
            }
        });
        view.startAnimation(animate);

    }

    // To animate view slide out from top to bottom
    public static void slideToBottom(View view) {
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, view.getHeight());
        animate.setDuration(DURATION);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }


    // To animate view slide out from bottom to top
    public static void slideToTop(View view) {
        TranslateAnimation animate = new TranslateAnimation(0, 0, 0, -view.getHeight());
        animate.setDuration(DURATION);
        animate.setFillAfter(true);
        view.startAnimation(animate);
        view.setVisibility(View.GONE);
    }

    public static void slideFromRight(final View view) {
        TranslateAnimation animate = new TranslateAnimation(SHIFT, 0, 0, 0);
        animate.setDuration(DURATION);
        animate.setFillAfter(true);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                view.setVisibility(View.VISIBLE);
                view.setEnabled(true);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // nothing to do
            }

            @Override
            public void onAnimationStart(Animation animation) {
                // nothing to do
                view.setEnabled(false);
            }
        });
        view.startAnimation(animate);

    }

    public static void slideFromLeft(final View view) {
        TranslateAnimation animate = new TranslateAnimation(-SHIFT, 0, 0, 0);
        animate.setDuration(DURATION);
        animate.setFillAfter(true);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                view.setVisibility(View.VISIBLE);
                view.setEnabled(true);
                //TODO customized function

            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // nothing to do
            }

            @Override
            public void onAnimationStart(Animation animation) {
                // nothing to do
                view.setEnabled(false);
            }
        });
        view.startAnimation(animate);

    }

    public static void moveToPosition(final View view, float position, final
    AfterAnimationListener afterAnimationListener) {
        TranslateAnimation animate = new TranslateAnimation(0, position - view.getX(), 0, 0);
        animate.setDuration(DURATION);
        animate.setFillAfter(true);
        animate.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationEnd(Animation animation) {
                view.clearAnimation();
                view.setVisibility(View.VISIBLE);
                view.setEnabled(true);
                //TODO customized function
                if (afterAnimationListener != null)
                    afterAnimationListener.afterAnimationCallback();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {
                // nothing to do
            }

            @Override
            public void onAnimationStart(Animation animation) {
                // nothing to do
                view.setEnabled(false);
            }
        });
        view.startAnimation(animate);

    }

    private static class ViewHolder {
        public boolean backOrientation;
        public boolean state;
        public float position;

    }
}
