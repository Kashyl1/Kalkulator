package calculator;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Kalkulator {
    private final String ZNAJDZ_REGEX = "(?i)^[a-z]+$";
    final String OPERAND = "(?i)^([+-]?\\d+|[a-z]+)$";

    private final Map<String, Integer> KOLEJNOSC = Map.of(
            "+", 1,
            "-", 1,
            "/", 2,
            "*", 2
    );

    private final Map<String, Integer> zmienne = new HashMap<>();

    public void run() {
        Scanner scanner = new Scanner(System.in);
        String input = scanner.nextLine().trim();
        Wybor wybor = Wybor.get(input);

        while (wybor != Wybor.EXIT) {
            try {
                switch (wybor) {
                    case HELP:
                    case UNKNOWN_COMMAND:
                        System.out.println(wybor.getWiadomosc());
                        break;
                    case CONTINUE:
                        if (input.matches(ZNAJDZ_REGEX)) {
                            System.out.println(getZmienna(input));
                        } else if (input.contains("=")) {
                            przypisz(input);
                        } else {
                            String postfix = prefixToPostfix(input);
                            System.out.println(processPostfix(postfix));
                        }
                        break;
                }
            } catch (RuntimeException e) {
                System.out.println(e.getMessage());
            }

            input = scanner.nextLine().trim();
            wybor = Wybor.get(input);
        }

        System.out.println(wybor.getWiadomosc());
    }

    private void przypisz(String input) {
        String[] args = input.split("\\s*=\\s*");
        final String PRZYPISZ_REGEX = "^([a-zA-z]+|-?\\d+)$";

        if (!args[0].matches(ZNAJDZ_REGEX)) {
            throw new RuntimeException(Error.INVALID_IDENTIFIER.getWiadomosc());
        }

        if (args.length > 2 || !args[1].matches(PRZYPISZ_REGEX)) {
            throw new RuntimeException(Error.INVALID_ASSIGNMENT.getWiadomosc());
        }

        int wartosc = getValue(args[1]);
        zmienne.put(args[0], wartosc);
    }

    private int getValue(String input) {
        int value;
        try {
            value = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            value = getZmienna(input);
        }
        return value;
    }
    private int getZmienna(String key) {
        Optional<Integer> value = Optional.ofNullable(zmienne.get(key));
        return value.orElseThrow(() ->
                new RuntimeException(Error.UNKNOWN_VARIABLE.getWiadomosc()));
    }

    private String prefixToPostfix(String input) {
        final String INVALID_INPUT = "(?i)((/(?=/))|(\\*(?=\\*))|[a-z]+\\d|\\d[a-z]+|[^ a-z()\\d/*+-])";
        if (input.matches(INVALID_INPUT)) {
            throw new RuntimeException(Error.INVALID_EXPRESSION.getWiadomosc());
        }
        final String zapytanie = "(?i)([a-z]+|(?<=^|\\s|\\()-?\\d+|\\d+|\\++|-+|\\*|/|\\(|\\))";
        Deque<String> stos = new ArrayDeque<>();
        Matcher matcher = Pattern.compile(zapytanie).matcher(input);
        StringBuilder postfix = new StringBuilder();

        matcher.results().forEach(m -> {
            if (isOperand(m.group())) {
                postfix.append(m.group()).append(" ");
            } else {
                if ("(".equals(m.group())) {
                    stos.offerLast(m.group());
                } else if (")".equals(m.group())) {
                    boolean foundPair = false;

                    while (!stos.isEmpty() && !foundPair) {
                        postfix.append(stos.pollLast()).append(" ");
                        foundPair = "(".equals(stos.peekLast());
                    }

                    if (!foundPair) {
                        throw new RuntimeException(Error.INVALID_EXPRESSION.getWiadomosc());
                    }

                    stos.pollLast();
                } else {
                    String operator = simplifyOperator(m.group());

                    if (stos.isEmpty() || "(".equals(stos.peekLast()) ||
                            KOLEJNOSC.get(operator) > KOLEJNOSC.get(stos.peekLast())) {
                        stos.offerLast(operator);
                    } else if (KOLEJNOSC.get(operator) <= KOLEJNOSC.get(stos.peekLast())) {
                        while (!stos.isEmpty() && !"(".equals(stos.peekLast()) &&
                                KOLEJNOSC.get(operator) <= KOLEJNOSC.get(stos.peekLast())) {
                            postfix.append(stos.pollLast()).append(" ");
                        }
                        stos.offerLast(operator);
                    }
                }
            }
        });

        while (!stos.isEmpty()) {
            postfix.append(stos.pollLast()).append(" ");
        }
        return postfix.toString().trim();
    }

    private int processPostfix(String input) {
        String[] wartosc = input.split("\\s+");
        Deque<Integer> stack = new ArrayDeque<>();
        for (String v: wartosc) {
            if (v.matches(OPERAND)) {
                stack.offerLast(getValue(v));
            } else {
                int num1 = Optional.ofNullable(stack.pollLast())
                        .orElseThrow(() -> new RuntimeException(Error.INVALID_EXPRESSION.getWiadomosc()));

                int num2 = Optional.ofNullable(stack.pollLast())
                        .orElseThrow(() -> new RuntimeException(Error.INVALID_EXPRESSION.getWiadomosc()));
                switch (v) {
                    case "+":
                        stack.offerLast(num2 + num1);
                        break;
                    case "-":
                        stack.offerLast(num2 - num1);
                        break;
                    case "*":
                        stack.offerLast(num2 * num1);
                        break;
                    case "/":
                        stack.offerLast(num2 / num1);
                        break;
                    default:
                        throw new RuntimeException(Error.INVALID_EXPRESSION.getWiadomosc());
                }
            }
        }
        return Optional.ofNullable(stack.pollLast())
                .orElseThrow(() -> new RuntimeException(Error.INVALID_EXPRESSION.getWiadomosc()));
    }
    private boolean isOperand(String wartosc) {
        final String OPERAND = "(?i)^([a-z]+|-?\\d+)$";
        return wartosc.matches(OPERAND);
    }

    private String simplifyOperator(String op) {
        final String MULTI_ADD = "^\\++$";
        final String MULTI_SUB = "^-+$";
        final String MULT_OR_DIV = "^([*/])$";

        if (op.matches(MULTI_ADD)) {
            return "+";
        } else if (op.matches(MULTI_SUB)) {
            return op.length() % 2 == 0 ? "+" : "-";
        } else if (op.matches(MULT_OR_DIV)) {
            return op;
        } else {
            throw new RuntimeException(Error.INVALID_EXPRESSION.getWiadomosc());
        }
    }
}