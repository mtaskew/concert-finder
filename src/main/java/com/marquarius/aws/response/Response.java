package com.marquarius.aws.response;

/**
 * Created by marquariusaskew on 5/7/17.
 */
public class Response {
    private DialogAction dialogAction;

    public DialogAction getDialogAction() {
        return dialogAction;
    }

    public void setDialogAction(DialogAction dialogAction) {
        this.dialogAction = dialogAction;
    }

    public static Response generateFulfilledResponse(String responseMessage) {
        Response response = new Response();
        Message message = new Message();
        message.setContent(responseMessage);
        message.setContentType("PlainText");
        DialogAction dialogAction = new DialogAction();
        dialogAction.setMessage(message);
        dialogAction.setFulfillmentState("Fulfilled");
        dialogAction.setType("Close");
        response.setDialogAction(dialogAction);

        return response;
    }

    public static Response generateFollowupStateResponse(String artistName) {
        Response response = new Response();
        Message message = new Message();
        message.setContent(artistName + "is not coming to anytime soon");
        message.setContentType("PlainText");

        DialogAction dialogAction = new DialogAction();
        dialogAction.setType("Delegate");
        dialogAction.setMessage(message);
        response.setDialogAction(dialogAction);

        return response;
    }
}
