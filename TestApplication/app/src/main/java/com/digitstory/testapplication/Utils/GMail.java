package com.digitstory.testapplication.Utils;

import java.io.UnsupportedEncodingException;
import java.util.List;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import android.app.AlertDialog.Builder;
import android.util.Log;

public class GMail {

    final String emailPort = "587";// gmail's smtp port
    final String smtpAuth = "true";
    final String starttls = "true";
    final String emailHost = "smtp.gmail.com";

    public static final String GMAIL_ID = "adam2020pd@gmail.com";
    public final  static String GMAIL_PWD = "Learner#12";
    public static final String GMAIL_SUB = "ALERT_SENSITIVE_ACTIVITY_FOUND";


    String fromEmail;
    String fromPassword;
    @SuppressWarnings("rawtypes")
    String toEmailList;
    String emailSubject;
    String emailBody;

    Properties emailProperties;
    Session mailSession;
    MimeMessage emailMessage;

    public GMail() {

    }

    @SuppressWarnings("rawtypes")
    public GMail(String fromEmail, String fromPassword,
            String toEmailList, String emailSubject, String emailBody) {
        this.fromEmail = fromEmail != null ? fromEmail : GMAIL_ID;
        this.fromPassword = fromPassword != null ? fromPassword : GMAIL_PWD;
        this.toEmailList = this.fromEmail;
        this.emailSubject = emailSubject != null ? getSubject(emailSubject) : GMAIL_SUB;
        this.emailBody = emailBody == null ? "NO_EMAIL_BODY_FOUND" : emailBody;

        emailProperties = System.getProperties();
        emailProperties.put("mail.smtp.port", emailPort);
        emailProperties.put("mail.smtp.auth", smtpAuth);
        emailProperties.put("mail.smtp.starttls.enable", starttls);
        Log.d(Config.TAG, "Mail server properties set.");
    }

    public MimeMessage createEmailMessage(String subjectPrefix) throws AddressException,
            MessagingException, UnsupportedEncodingException {

        mailSession = Session.getInstance(emailProperties, new javax.mail.Authenticator() {
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(fromEmail, fromPassword);
            }
        });
        emailMessage = new MimeMessage(mailSession);

        emailMessage.setFrom(new InternetAddress(fromEmail, fromEmail));

            Log.d(Config.TAG,"toEmail: "+toEmailList);
            emailMessage.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(toEmailList));


        emailMessage.setSubject(subjectPrefix + emailSubject);
       // emailMessage.setContent(emailBody, "text/html");// for a html email
         emailMessage.setText(emailBody);// for a text email
        Log.d(Config.TAG, "Email Message created.");
        return emailMessage;
    }

    public void sendEmail(String subjectPrefix) throws AddressException, MessagingException, UnsupportedEncodingException {
        try {
            emailMessage = createEmailMessage(subjectPrefix);
            Transport transport = mailSession.getTransport("smtp");
            transport.connect(emailHost, fromEmail, fromPassword);

            Log.d(Config.TAG, "sendEmail: " + getCompleteInfo());
            transport.sendMessage(emailMessage, emailMessage.getAllRecipients());
            transport.close();
            Log.d(Config.TAG, "Email sent successfully.");
        }catch(Exception ex){
            Log.d(Config.TAG, "Looks like internet connection is not there");
        }

    }



    public static String getSubject(String number){
        return GMAIL_SUB + ": " + number;
    }

    private String getCompleteInfo(){
        StringBuilder  sb = new StringBuilder();
        return sb.append("RECIPIENT:" + toEmailList)
                 .append("\n")
                .append("EMAIL_SUBJECT: " + emailSubject)
                .append("\n")
                .append("EMAIL_BODY: " + emailBody).toString();
    }


}