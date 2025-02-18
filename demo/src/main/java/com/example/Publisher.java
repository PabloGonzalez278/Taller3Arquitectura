package com.example;

import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.*;

import com.sendgrid.*;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.*;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

import java.io.IOException;

public class Publisher {
    private static final String AWS_TOPIC_NAME = "CompraNotificacion";
    private static SnsClient snsClient = SnsClient.create();
    private static String topicArn;

    public static void main(String[] args) {
        topicArn = createTopic(AWS_TOPIC_NAME);
        while (true) {
            String message = "Nueva compra realizada. ID: " + System.currentTimeMillis();
            System.out.println("Publicando mensaje: " + message);
            publishMessage(topicArn, message);
            sendEmailNotification(message);
            sendSmsNotification(message);

            try {
                Thread.sleep(5000); // Simula la publicación cada 5 segundos
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static String createTopic(String topicName) {
        CreateTopicRequest request = CreateTopicRequest.builder().name(topicName).build();
        CreateTopicResponse response = snsClient.createTopic(request);
        System.out.println("Tópico creado: " + response.topicArn());
        return response.topicArn();
    }

    public static void publishMessage(String topicArn, String message) {
        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .build();

        snsClient.publish(request);
        System.out.println("Mensaje publicado en el tópico.");
    }

    public static void sendEmailNotification(String message) {
        Email from = new Email("noreply@example.com");
        String subject = "Notificación de Compra";
        Email to = new Email("usuario@example.com");
        Content content = new Content("text/plain", message);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid("SENDGRID_API_KEY");
        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            sg.api(request);
            System.out.println("Correo enviado.");
        } catch (IOException ex) {
            System.err.println("Error enviando correo: " + ex.getMessage());
        }
    }

    public static void sendSmsNotification(String message) {
        Twilio.init("TWILIO_ACCOUNT_SID", "TWILIO_AUTH_TOKEN");
        Message sms = Message.creator(new PhoneNumber("+1234567890"),
                                      new PhoneNumber("+0987654321"),
                                      message)
                               .create();
        System.out.println("SMS enviado: " + sms.getSid());
    }
}