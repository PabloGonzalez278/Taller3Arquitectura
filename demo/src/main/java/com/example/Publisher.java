package com.example;

import java.io.IOException;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.PublishRequest;
import software.amazon.awssdk.services.sns.model.PublishResponse;
import com.sendgrid.Method;
import com.sendgrid.Request;
import com.sendgrid.SendGrid;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

/**
 * Clase Publisher para enviar notificaciones a AWS SNS.
 */
public class Publisher {
    private static final int WAIT_TIME_MS = 5000;
    private static final String TWILIO_ACCOUNT_SID = "TU_TWILIO_SID";
    private static final String TWILIO_AUTH_TOKEN = "TU_TWILIO_AUTH_TOKEN";
    private static final String SENDGRID_API_KEY = "TU_SENDGRID_API_KEY";
    private static final String TOPIC_NAME = "CompraNotificacion";

    private final SnsClient snsClient;
    private final String topicArn;

    /**
     * Constructor de la clase Publisher.
     */
    public Publisher() {
        this.snsClient = SnsClient.create();
        this.topicArn = createTopic();
    }

    /**
     * Método principal para ejecutar el publicador.
     * @param args Argumentos de la línea de comandos.
     */
    public static void main(String[] args) {
        Publisher publisher = new Publisher();
        while (true) {
            String message = "Nueva compra realizada. ID: " + System.currentTimeMillis();
            System.out.println("Publicando mensaje: " + message);
            publisher.publishMessage(message);
            publisher.sendEmailNotification(message);
            publisher.sendSmsNotification(message);

            try {
                Thread.sleep(WAIT_TIME_MS);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }
    }

    private String createTopic() {
        CreateTopicRequest request = CreateTopicRequest.builder().name(TOPIC_NAME).build();
        CreateTopicResponse response = snsClient.createTopic(request);
        System.out.println("Tópico creado: " + response.topicArn());
        return response.topicArn();
    }

    private void publishMessage(String message) {
        PublishRequest request = PublishRequest.builder()
                .topicArn(topicArn)
                .message(message)
                .build();

        PublishResponse response = snsClient.publish(request);
        System.out.println("Mensaje publicado con ID: " + response.messageId());
    }

    private void sendEmailNotification(String message) {
        Email from = new Email("noreply@example.com");
        String subject = "Notificación de Compra";
        Email to = new Email("usuario@example.com");
        Content content = new Content("text/plain", message);
        Mail mail = new Mail(from, subject, to, content);

        SendGrid sg = new SendGrid(SENDGRID_API_KEY);
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

    private void sendSmsNotification(String message) {
        Twilio.init(TWILIO_ACCOUNT_SID, TWILIO_AUTH_TOKEN);
        Message.creator(new PhoneNumber("+1234567890"),
                new PhoneNumber("+0987654321"),
                message)
                .create();
        System.out.println("SMS enviado.");
    }
}
