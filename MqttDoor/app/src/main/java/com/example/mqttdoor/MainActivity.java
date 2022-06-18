package com.example.mqttdoor;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.io.UnsupportedEncodingException;
import java.util.concurrent.TimeUnit;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

    //Declaração de variáveis para estabelecer conexão
    static String MQTTHOST = "tcp://clistenes.cloud.shiftr.io";
    static int PORT = 1883;
    static String USERNAME = "clistenes";
    static String PASSWORD = "W1WvikFZ35ZafR1s";
    //String topicStr = "LED";
    MqttAndroidClient client;
    //------------
    public static final String TITULO_APPBAR = "ESQUECI A PORTA ABERTA?";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(TITULO_APPBAR);
        setContentView(R.layout.activity_main);
        TextView sensor_status = findViewById(R.id.exibeStatusPorta);
        String sub_topic = "porta/sensor_status";

        // Gerar o Id do cliente para fazer a conexão
        String clientId = MqttClient.generateClientId();
        client = new MqttAndroidClient(this.getApplicationContext(), MQTTHOST, clientId);

        MqttConnectOptions options = new MqttConnectOptions();
        options.setUserName(USERNAME);
        options.setPassword(PASSWORD.toCharArray());


        try {
            //IMqttToken token = client.connect(options);
            //token.setActionCallback(new IMqttActionListener() {
            client.connect(options, null, new IMqttActionListener() {
            @Override
            public void onSuccess(IMqttToken asyncActionToken) {
                Toast.makeText(MainActivity.this, "Conectado", Toast.LENGTH_SHORT).show();
                setSubscription();
            }

            @Override
            public void onFailure(IMqttToken asyncActionToken, Throwable exception) {
                Toast.makeText(MainActivity.this, "falha na conexão", Toast.LENGTH_SHORT).show();
            }
        });

        } catch (MqttException e) {
            e.printStackTrace();
        }
        //subscriber
        client.setCallback(new MqttCallback() {
            @Override
            public void connectionLost(Throwable cause) {
                Toast.makeText(MainActivity.this, "Conexão perdida", Toast.LENGTH_SHORT);
            }

            @Override
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                Toast.makeText(MainActivity.this, "tópico: "+ topic + "mensagem: " + new String(message.getPayload()), Toast.LENGTH_SHORT);

                if(topic.toString().equals("porta/sensor_status")){
                    sensor_status.setText(new String(message.getPayload()));
                }
            }

            @Override
            public void deliveryComplete(IMqttDeliveryToken token) {
                Toast.makeText(MainActivity.this, "Mensagem entregue", Toast.LENGTH_SHORT);
            }
        });

        //publisher
        Button unlockButton = findViewById(R.id.btn_open_door_activity);
        Button verifyRequestButton = findViewById(R.id.btn_status_door_activity);
        unlockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String topic = "porta/atuador";
                String payload = "1";
                byte[] encodedPayload = new byte[0];
                try {
                    encodedPayload = payload.getBytes("UTF-8");
                    MqttMessage message = new MqttMessage(encodedPayload);
                    client.publish(topic, message);
                    lockdoor();

                } catch (UnsupportedEncodingException | MqttException | InterruptedException e) {
                    e.printStackTrace();
                }
            }

        });
        verifyRequestButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String topic = "porta/sensor_request";
                String payload = "1";
                byte[] encodedPayload = new byte[0];
                try {
                    encodedPayload = payload.getBytes("UTF-8");
                    MqttMessage message = new MqttMessage(encodedPayload);
                    client.publish(topic, message);
                } catch (UnsupportedEncodingException | MqttException e) {
                    e.printStackTrace();
                }
            }
        });
    }
    private void setSubscription(){

        try {
            client.subscribe("porta/sensor_status",1);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    private void lockdoor() throws InterruptedException {

        String topic = "porta/atuador";
        String payload = "0";
        byte[] encodedPayload = new byte[0];
        Thread.sleep(2000);
        try {
            encodedPayload = payload.getBytes("UTF-8");
            MqttMessage message = new MqttMessage(encodedPayload);
            client.publish(topic, message);
        } catch (UnsupportedEncodingException | MqttException e) {
            e.printStackTrace();
        }
    }
}