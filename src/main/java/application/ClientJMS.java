package application;

import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

public class ClientJMS extends Application {
	
	private Session session;
	private MessageProducer messageProducer;
	private MessageConsumer messageConsumer;
	private ObservableList<String> observableList;

	public static void main(String[] args) {
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) {
		initialize();
		primaryStage.setTitle("Client JMS");
		BorderPane borderPane = new BorderPane();
		VBox vBox = new VBox();		
		GridPane gridPane = new GridPane();
		gridPane.setVgap(10);
		gridPane.setHgap(10);
		gridPane.setPadding(new Insets(10));
		
		Label labelLastName = new Label("Last Name:");
		TextField txtFLastName = new TextField();
		
		Label labelFirstName = new Label("First Name:");
		TextField txtFFirstName = new TextField();
		
		Button registerButton = new Button("Register");
		gridPane.add(labelLastName, 0, 0);
		gridPane.add(txtFLastName, 1, 0);
		gridPane.add(labelFirstName, 0, 1);
		gridPane.add(txtFFirstName, 1, 1);
		gridPane.add(registerButton, 0, 2);
		vBox.getChildren().add(gridPane);
		vBox.setPadding(new Insets(10));
		vBox.setSpacing(10);
		
		observableList = FXCollections.observableArrayList();		
		ListView<String> listView = new ListView<>(observableList);
		
		
		vBox.getChildren().add(listView);
		borderPane.setCenter(vBox);
		Scene scene = new Scene(borderPane, 400, 300);
		primaryStage.setScene(scene);
		primaryStage.show();
		
		registerButton.setOnAction( s -> {
			try {
				String lastName = txtFLastName.getText();
				String firstName = txtFFirstName.getText();
				
				TextMessage textMessage = session.createTextMessage();
				textMessage.setText(lastName + "_" + firstName);
				messageProducer.send(textMessage);
			} catch (JMSException e) {

				e.printStackTrace();
			}
		});
	}
	
	public void initialize() {
		try {
			ConnectionFactory connectionFactory = new ActiveMQConnectionFactory("tcp://localhost:61616");
			Connection connection = connectionFactory.createConnection();
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			connection.start();
			Destination destination = session.createQueue("schooling.queue");
			messageProducer = session.createProducer(destination);
			
			Destination destination2 = session.createQueue("toClient.queue");
			messageConsumer = session.createConsumer(destination2);
			messageConsumer.setMessageListener(new MessageListener() {				
				
				@Override
				public void onMessage(Message message) {
					if(message instanceof TextMessage) {
						try {
							String content = ((TextMessage) message).getText();
							observableList.add(content);
						} catch (JMSException e) {
							e.printStackTrace();
						}
					}				
				}
			});
			
		} catch (JMSException e) {
			e.printStackTrace();
		}
	}

}
