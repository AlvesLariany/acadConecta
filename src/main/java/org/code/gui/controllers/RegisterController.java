package org.code.gui.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import org.code.gui.util.Alerts;
import org.code.gui.util.HashUtil;
import org.code.gui.util.LoadViewInPane;
import org.code.model.entities.User;
import org.code.persistence.DataService;
import org.w3c.dom.Text;

public class RegisterController {
    private final int TAM_INFO_USER = 3;
    private final int MAX_HASH_USER = 2;
    private final int USER_NAME = 0;
    private final int USER_EMAIL = 1;
    private final int USER_PASSWORD = 2;
    @FXML
    private TextField nameUser;
    @FXML
    private TextField emailUser;
    @FXML
    private PasswordField passwordUser;

    private String[] getInformationUser() {
        String[] informationUser = new String[TAM_INFO_USER];
        informationUser[USER_NAME] = nameUser.getText();
        informationUser[USER_EMAIL] = emailUser.getText();
        informationUser[USER_PASSWORD] = passwordUser.getText();

        return informationUser;
    }

    public boolean isHaveNull(String[] information) {
        for (String info : information) {
            if (info.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    public String[] makeHashOfEmailAndPassword(String email, String password) {
        String[] hashes = new String[MAX_HASH_USER];

        hashes[0] = HashUtil.defineHash(email);
        hashes[1] = HashUtil.defineHash(password);

        return hashes;
    }

    @FXML
    public void onButtonCancelClicked() {
        LoadViewInPane.loadView("LoginView.fxml", null, "Login");

        Alerts.showAlert("Criação interrompida", null, "A criação da conta foi cancelada pelo usuário", Alert.AlertType.INFORMATION);
    }

    @FXML
    public void onButtonCreateClicked() {
        String[] informationUser = new String[TAM_INFO_USER];
        informationUser = getInformationUser();

        if (isHaveNull(informationUser)) {
            //quando criar o usuário, verificar no banco se o email já foi registrado
            //mandar senha e email criptografados para o banco de dados, realizar comparação na consulta
            //criar termos de uso da plataforma

            String[] hashes = makeHashOfEmailAndPassword(informationUser[USER_EMAIL], informationUser[USER_PASSWORD]);

            if (DataService.saveItem(new User(informationUser[USER_NAME], hashes[0], hashes[1]))) {
                Alerts.showAlert("Conta criada", null, "Conta criada com sucesso, realize o login para aproveitar a plataforma", Alert.AlertType.CONFIRMATION);
            }
            else {
                Alerts.showAlert("Falha", null, "Erro ao criar conta, verifique os campos e tente novamente. Caso o problema persista, tente novamente mais tarde", Alert.AlertType.ERROR);
            }

        }
        else {
            Alerts.showAlert("Falha", null, "Error ao criar conta, não é permitido que os campos (nome, email ou senha) fiquem vazios, tente novamente", Alert.AlertType.ERROR);
        }
        LoadViewInPane.loadView("LoginView.fxml", null, "Login");
    }
}