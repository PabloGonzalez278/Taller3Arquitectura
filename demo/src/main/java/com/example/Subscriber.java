package com.example;

import java.io.IOException;
import software.amazon.awssdk.services.sns.SnsClient;
import software.amazon.awssdk.services.sns.model.CreateTopicRequest;
import software.amazon.awssdk.services.sns.model.CreateTopicResponse;
import software.amazon.awssdk.services.sns.model.SubscribeRequest;
import software.amazon.awssdk.services.sns.model.SubscribeResponse;
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
 * Clase Subscriber para recibir notificaciones desde AWS SNS.
 */
public class Subscriber {
    private static final String TWILIO_ACCOUNT_SID = "TU_TWILIO_SID";
    private static final String TWILIO_AUTH_TOKEN = "TU_TWILIO_AUTH_TOKEN";
    private static final String SENDGRID_API_KEY = "TU_SENDGRID_API_KEY";
    private static final String TOPIC_NAME = "CompraNotificacion";

    private final SnsClient snsClient;
    private final String topicArn;

    /**
     * Constructor de la clase Subscriber.
     */
    public Subscriber() {
        this.snsClient = SnsClient.create();
        this.topicArn = createTopic();
        subscribeToTopic();
    }

    /**
     * Método principal que inicia el suscriptor.
     * @param args Argumentos de línea de comandos.
     */
    public static void main(String[] args) {
        new Subscriber();
        System.out.println("Esperando mensajes...");
    }

    private String createTopic() {
        CreateTopicRequest request = CreateTopicRequest.builder().name(TOPIC_NAME).build();
        CreateTopicResponse response = snsClient.createTopic(request);
        System.out.println("Tópico creado: " + response.topicArn());
        return response.topicArn();
    }

    private void subscribeToTopic() {
        SubscribeRequest request = SubscribeRequest.builder()
                .topicArn(topicArn)
                .protocol("email")
                .endpoint("usuario@example.com")
                .build();

        SubscribeResponse response = snsClient.subscribe(request);
        System.out.println("Suscripción creada con ARN: " + response.subscriptionArn());
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
