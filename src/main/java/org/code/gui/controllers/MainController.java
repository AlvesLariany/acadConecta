package org.code.gui.controllers;

import javafx.animation.FadeTransition;
import javafx.animation.RotateTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import org.code.gui.util.Alerts;
import org.code.gui.util.ImageUtil;
import org.code.model.entities.Chanel;
import org.code.model.entities.Message;
import org.code.model.entities.Users;
import org.code.model.util.TokenChanelUtil;
import org.code.model.util.TokenUserUtil;
import org.code.persistence.DataService;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class MainController {
    protected static final Long EDITAIS = 1L;
    protected static final Long VAGAS = 2L;
    protected static final Long CHAT = 3L;
    private boolean orientation = true;

    @FXML
    private ImageView imageView;

    @FXML
    private Label titlePanel;

    @FXML
    private ScrollPane scrollPaneMain;

    @FXML
    private VBox contentScrollPane;

    @FXML
    private HBox chatBar;

    @FXML
    private TextField contentChatBar;

    @FXML
    private VBox expandedMenu;

    @FXML
    private void toggleMenu() {
        FadeTransition ft = new FadeTransition(Duration.millis(300), expandedMenu);

        RotateTransition rotateTransition = new RotateTransition();

        rotateTransition.setNode(imageView);
        rotateTransition.setDuration(Duration.seconds(.3));
        rotateTransition.setCycleCount(1);
        rotateTransition.setAutoReverse(false);

        if (orientation) {
            rotateTransition.setByAngle(90);
            orientation = !orientation;
        }
        else {
            rotateTransition.setByAngle(-90);
            orientation = !orientation;
        }


        rotateTransition.play(); // Iniciar a rotação

        if (expandedMenu.isVisible()) {
            ft.setFromValue(1.0);
            ft.setToValue(0.0);
            ft.setOnFinished(e -> expandedMenu.setVisible(false));
        } else {
            expandedMenu.setVisible(true);
            ft.setFromValue(0.0);
            ft.setToValue(1.0);
        }

        ft.play();
        rotateTransition.play();
    }

    private void setTitleChanel(Long id) {
        if (titlePanel.isVisible()) {
            String title = DataService.getTitleChanel(id);
            if (title != null) {
                titlePanel.setText(title);
            }
            else {
                System.out.println("Erro ao carregar titulo da página");
            }
        }
    }

    private void loadInView(List<Message> messages) {
        messages.forEach(message -> {
            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/gui/views/components/MessageView.fxml"));
                VBox vBox = fxmlLoader.load();

                MessageControl messageControl = fxmlLoader.getController();

                //pegando usuário associado a mensagem
                Users users = DataService.findByHashEmail(message.getId_users().getEmail());

                /* GARANTIR QUE TODOS OS USUÁRIOS RECEM CRIADOS POSSUAM UMA IMAGEM PADRÃO */
                Image image = ImageUtil.getImageWithEmailUser(users.getEmail());

                image = image != null ? image : null;

                System.out.println(image.getUrl());

                //adicionando o conteudo do banco na mensagem
                messageControl.setContent(users.getName(), message.getDate().toString(), message.getHour().toString(), message.getContent(), image);

                //adicionando a mensagem no scrollpane
                if (vBox != null) {
                    contentScrollPane.getChildren().add(vBox);
                }

            } catch (IOException error) {
                System.out.println("Error in load message: " + error.getMessage());
            }
        });
    }

    private void loadMessagesInChanel(Long id) {
        Chanel chanel = DataService.findByChanelId(id);

        List<Message> messages = DataService.findMessageById(chanel);

        clearScrollPane();
        loadInView(messages);
    }

    private void clearScrollPane() {
        if (contentScrollPane != null) {
            contentScrollPane.getChildren().clear();
        }
    }

    private void checkAndSetChatBar() {
        if (titlePanel != null) {
            chatBar.setVisible(titlePanel.getText().equals("CHAT"));
        }
    }

    private void preparateView(Long chanelId) {
        //define o titulo da página
        setTitleChanel(chanelId);

        //define o canal atual
        TokenChanelUtil.setToken(chanelId);

        //limpa a tela
        clearScrollPane();

        //verificar se o canal é o chat, permite acessoa a barra de digitação
        checkAndSetChatBar();

        //carrega as mensagens do canal clicado
        loadMessagesInChanel(chanelId);
    }

    @FXML
    public void onEnditaisButtonClicked() {
        preparateView(EDITAIS);
        //carregar mensagens do banco com base no ID do canal (Editais)
    }

    @FXML
    public void onChatButtonClicked() {
        preparateView(CHAT);

        //carregar mensagens do banco com base no ID do canal (Editais)
    }

    @FXML
    public void onEmployeeButtonClicked() {
        preparateView(VAGAS);
        //carregar mensagens do banco com base no ID do canal (Editais)
    }

    @FXML
    public void onUserSubmitMessage() {
        String input = contentChatBar.getText();

        if (input.isEmpty()) {
            Alerts.showAlert("Falha", null, "Erro ao enviar mensagem, campo está vazio", Alert.AlertType.WARNING);
        }
        else {
            //usuario que enviou
            Users users = DataService.findByHashEmail(TokenUserUtil.getUserToken());
            Chanel chanel = DataService.findByChanelId(TokenChanelUtil.getToken());

            if (users != null && chanel != null) {
                Message message = new Message(null, LocalTime.now(), LocalDate.now(), input, users, chanel);

                DataService.saveItem(message);

                loadMessagesInChanel(CHAT);
                contentChatBar.setText("");
                scrollPaneMain.setVvalue(1.0);
            }
            else {
                Alerts.showAlert("Erro", null, "Erro ao enviar a mensagem, tente novamente", Alert.AlertType.WARNING);
            }

        }

    }

    private void onEnterPressed(KeyEvent keyEvent) {
        if (keyEvent.getCode() == KeyCode.ENTER) {
            onUserSubmitMessage();
        }
    }

    /*TODO: Reduzir codigo, funções iguais chamadar da mesma forma nos métodos
    *  verificar se o scroll pane foi até o seu limite inferior
    * pensar no perfil dos usuários especiais */
    @FXML
    private void initialize() {
        setTitleChanel(EDITAIS);
        TokenChanelUtil.setToken(EDITAIS);

        checkAndSetChatBar();
        //carregar mensagens do banco com base no ID do canal (Editais)

        contentChatBar.setOnKeyPressed(event -> onEnterPressed(event));
    }
}
