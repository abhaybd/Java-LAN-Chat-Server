import java.io.IOException;

import javafx.application.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class ClientGUI extends Application {
	private TextField textBar;
	private Button sendButton;
	private Button reconnect;
	private boolean connected;
	private ScrollPane scroll;
	
	public Text chat;
	public Client client;
	
	public static void main(String[] args){
		launch(args);
	}
	
	void setUpClient(){
		chat.setText("Searching for server on network.\nThis may take a while.");
		client = new Client(this);
		System.out.println("Scanning subnet for server...");
		Runnable run = new Runnable(){
			@Override
			public void run() {
				try {
					String ip = Client.getServer();
					System.out.println("Server found: " + ip);
					Platform.runLater(() -> chat.setText(""));
					if(ip != null) client.connect(ip);
					else Platform.runLater(() -> chat.setText("Server not online! Try again later!"));
				} catch (IOException e) {
					//e.printStackTrace();
				}
			}
		};
		Thread t = new Thread(run);
		t.start();
	}
	
	public void display(String s){
		//chat.setText(chat.getText() + "\n" + s);
		javafx.application.Platform.runLater(() -> chat.setText(chat.getText() + "\n" + s));
		scroll.setVvalue(scroll.getVmax());
		new Thread(new Runnable(){
			@Override
			public void run() {
				try {
					Thread.sleep(100);
					scroll.setVvalue(scroll.getVmax());
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
			
		}).start();;
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		connected = false;
		textBar = new TextField();
		sendButton = new Button("Send");
		reconnect = new Button("Connect");
		reconnect.setPrefWidth(200);
		HBox topButton = new HBox();
		topButton.setAlignment(Pos.BASELINE_CENTER);
		topButton.getChildren().add(reconnect);
		chat = new Text();
		textBar.setPrefSize(500, textBar.getHeight());
		stage.setTitle("Le Chat Server");
		stage.setWidth(1024);
		stage.setHeight(768);
		stage.setResizable(false);
		Scene scene = new Scene(new Group());
		VBox root = new VBox();
		HBox inputLayout = new HBox();
		inputLayout.setAlignment(Pos.BASELINE_CENTER);
		scroll = new ScrollPane();
		StackPane stack = new StackPane();
		scroll.setPrefSize(768, 800);
		stack.setAlignment(Pos.TOP_LEFT);
		stack.getChildren().addAll(chat);
		scroll.setStyle("-fx-background:black;");
		scroll.setContent(stack);
		chat.setFill(Color.GREEN);
		inputLayout.getChildren().addAll(textBar, sendButton);
		root.getChildren().addAll(topButton,scroll,inputLayout);
		scene.setRoot(root);
		stage.setScene(scene);
		stage.show();
		stage.setOnCloseRequest(e -> System.exit(0));
		buttonListeners();
	}
	
	void buttonListeners(){
		sendButton.setOnAction(e -> send());
		textBar.setOnKeyPressed(new EventHandler<KeyEvent>(){
			@Override
			public void handle(KeyEvent event) {
				if(event.getCode().equals(KeyCode.ENTER)){
					send();
				}
			}
		});
		reconnect.setOnAction(new EventHandler<ActionEvent>(){
			@Override
			public void handle(ActionEvent arg0) {
				connected = !connected;
				if(connected){
					setUpClient();
					reconnect.setText("Disconnect and reconnect.");
				}
				if(!connected){
					client.close();
					setUpClient();
					reconnect.setText("Disconnect and reconnect.");
				}
			}
			
		});
	}
	
	private void send(){
		client.send(textBar.getText());
		textBar.setText("");
	}
}