package ma.emsi.daha.pe2rigp2.jsf;


import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.model.SelectItem;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import ma.emsi.daha.pe2rigp2.llm.LlmClient;

import java.io.Serializable;
import java.util.*;

@Named
@ViewScoped
public class ChatBDAHA implements Serializable {

    private String systemRole;
    private boolean systemRoleChangeable = true;
    private String question;
    private String reponse;
    private StringBuilder conversation = new StringBuilder();
    private boolean debug;

    @Inject
    private FacesContext facesContext;
    @Inject
    private LlmClient llmClient;

    public ChatBDAHA() {
    }

    public String getSystemRole() {
        return systemRole;
    }

    public void setSystemRole(String systemRole) {
        this.systemRole = systemRole;
    }

    public boolean isSystemRoleChangeable() {
        return systemRoleChangeable;
    }

    public String getQuestion() {
        return question;
    }

    public void setQuestion(String question) {
        this.question = question;
    }

    public String getReponse() {
        return reponse;
    }

    public void setReponse(String reponse) {
        this.reponse = reponse;
    }

    public String getConversation() {
        return conversation.toString();
    }

    public void setConversation(String conversation) {
        this.conversation = new StringBuilder(conversation);
    }

    public boolean isDebug() {
        return debug;
    }

    public void setDebug(Boolean debug) {
        this.debug = debug;
    }

    public void toggleDebug() {
        this.setDebug(!isDebug());
    }

    public String envoyer() {
        if (question == null || question.isBlank()) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Texte question vide", "Il manque le texte de la question");
            facesContext.addMessage(null, message);
            return null;
        }

        try {
            // Mise à jour du rôle système et de la mémoire
            llmClient.setSystemRole(systemRole);

            // Envoi de la question à l'API du LLM et récupération de la réponse
            this.reponse = llmClient.sendMessageToLlm(question);

            // Ajout de la réponse à la conversation
            afficherConversation();

        } catch (Exception e) {
            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Problème de connexion avec l'API du LLM",
                    "Problème de connexion avec l'API du LLM : " + e.getMessage());
            facesContext.addMessage(null, message);
        }
        return null;
    }

    public String nouveauChat() {
        return "index";
    }

    private void afficherConversation() {
        this.conversation.append("== User:\n").append(question).append("\n== Conseiller en bien-être:\n").append(reponse).append("\n\n");
    }

    public List<SelectItem> getSystemRoles() {
        List<SelectItem> listeSystemRoles = new ArrayList<>();

        String role = """
                You are a helpful assistant. You help the user to find the information they need.
                If the user types a question, you answer it.
                """;
        listeSystemRoles.add(new SelectItem(role, "Assistant"));

        role = """
                You are an interpreter. You translate from English to French and from French to English.
                If the user types a French text, you translate it into English.
                If the user types an English text, you translate it into French.
                If the text contains only one to three words, give some examples of usage of these words in English.
                """;
        listeSystemRoles.add(new SelectItem(role, "Traducteur Anglais-Français"));

        role = """
                You are a travel guide. If the user types the name of a country or of a town,
                you tell them what are the main places to visit in the country or the town
                and you tell them the average price of a meal.
                """;
        listeSystemRoles.add(new SelectItem(role, "Guide touristique"));

        // Ajout du rôle Conseiller en bien-être
        role = """
                You are a wellness advisor. If the user expresses feelings of stress or anxiety,
                you offer calming advice, breathing exercises, or tips to improve mental health.
                You encourage the user to take breaks, relax, and focus on positive thoughts.
                """;
        listeSystemRoles.add(new SelectItem(role, "Conseiller en bien-être"));

        this.systemRole = (String) listeSystemRoles.get(0).getValue(); // Initialisation avec le rôle par défaut
        return listeSystemRoles;
    }
}