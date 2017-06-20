package com.marquarius.aws.request;

/**
 * Created by marquariusaskew on 5/7/17.
 */
public class Request {
    private CurrentIntent currentIntent;

    public CurrentIntent getCurrentIntent() {
        return currentIntent;
    }

    public void setCurrentIntent(CurrentIntent currentIntent) {
        this.currentIntent = currentIntent;
    }
}
