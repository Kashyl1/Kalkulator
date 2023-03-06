package calculator;

public enum Wybor {
    CONTINUE(null, null),
    UNKNOWN_COMMAND(null, Error.UNKNOWN_COMMAND.getWiadomosc()),
    EMPTY("", null),
    HELP("/help", "The program will calculate expressions like these: 3 + 8 * ((4 + 3) * 2 + 1) - 6 / (2 + 1), and so on."),
    EXIT("/exit", "Bye!");

    final String option;
    final String wiadomosc;

    Wybor(String option, String wiadomosc) {
        this.option = option;
        this.wiadomosc = wiadomosc;
    }

    public static Wybor get(String input) {
        for (Wybor r : Wybor.values()) {
            if (input.equalsIgnoreCase(r.option)) {
                return r;
            }
        }

        return input.startsWith("/") ? UNKNOWN_COMMAND : CONTINUE;
    }

    public String getWiadomosc() {
        return wiadomosc;
    }
}