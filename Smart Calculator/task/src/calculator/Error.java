package calculator;

public enum Error {
    INVALID_IDENTIFIER("Invalid identifier"),
    INVALID_ASSIGNMENT("Invalid assignment"),
    INVALID_EXPRESSION("Invalid expression"),
    UNKNOWN_VARIABLE("Unknown variable"),
    UNKNOWN_COMMAND("Unknown command");

    private final String wiadomosc;

    Error(String message) {
        this.wiadomosc = message;
    }

    public String getWiadomosc() {
        return wiadomosc;
    }
}