
import java.io.*;
import java.util.*;

public class TuringMachine1 {
    private List<Transition> transitions = new ArrayList<>();
    private Set<String> finalStates = new HashSet<>();
    private LinkedList<Character> tape;
    private int headPosition;
    private String currentState;
    private static final String BLANK = "B";

    public static void main(String[] args) {
        TuringMachine1 tm = new TuringMachine1();

        try {
            // Load transitions and final states from the configuration file
            tm.loadConfiguration("turing_machine_config1.txt");

            // Print the encoding explanations
            tm.printEncodingExplanation();

            // Print the encoded transitions
            tm.printEncodedTransitions();

            // Take user input for the string to process
            Scanner scanner = new Scanner(System.in);
            System.out.print("Enter a string for the Turing Machine: ");
            String input = scanner.nextLine();

            // Run the machine and print the result
            boolean result = tm.run(input);
            System.out.println("The machine " + (result ? "accepts" : "rejects") + " the string.");
            scanner.close();
        } catch (IOException e) {
            System.err.println("Error loading transitions: " + e.getMessage());
        }
    }

    // Print the encoding explanation for states, symbols, and directions
    private void printEncodingExplanation() {
        System.out.println("Encoding Explanations:");
        System.out.println("q0: 111");
        System.out.println("q1: 1111");
        System.out.println("q2: 11111");
        System.out.println("0: 101");
        System.out.println("1: 1010");
        System.out.println("B: 111111");
        System.out.println("L: 1");
        System.out.println("R: 11");
        System.out.println(); // New line for better formatting
    }

    // Print the encoded transitions
    private void printEncodedTransitions() {
        StringBuilder encoded = new StringBuilder("0000");

        for (int i = 0; i < transitions.size(); i++) {
            Transition transition = transitions.get(i);

            // Encode each part of the transition
            encoded.append(encodeState(transition.currentState))
                   .append("00") // Encode current state
                   .append(encodeSymbol(transition.readSymbol))
                   .append("00") // Encode read symbol
                   .append(encodeState(transition.nextState))
                   .append("00") // Encode next state
                   .append(encodeSymbol(transition.writeSymbol))
                   .append("00") // Encode write symbol
                   .append(encodeDirection(transition.direction)); // Encode direction

            // Add "000" after each transition except the last one
            if (i < transitions.size() - 1) {
                encoded.append("000");
            }
        }

        encoded.append("0000");
        System.out.println("Encoded Transitions: " + encoded);
    }

    // Encode state names using predefined mappings
    private String encodeState(String state) {
        switch (state) {
            case "q0": return "111";
            case "q1": return "1111";
            case "q2": return "11111";
            case "q3": return "1111111";
            default: throw new IllegalArgumentException("Unknown state: " + state);
        }
    }

    // Encode symbols ('0', '1', B) using predefined mappings
    private String encodeSymbol(String symbol) {
        switch (symbol) {
            case "0": return "101";
            case "1": return "1010";
            case "B": return "111111";
            default: throw new IllegalArgumentException("Unknown symbol: " + symbol);
        }
    }

    // Encode direction ('L' for left, 'R' for right)
    private String encodeDirection(String direction) {
        return direction.equals("L") ? "1" : "11";
    }

    // Load transition configuration and final states from the file
    private void loadConfiguration(String filename) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(filename))) {
            String line;
            boolean isTransitionSection = false;

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                // Ignore comments and empty lines
                if (line.startsWith("#") || line.isEmpty()) continue;

                // Process final states
                if (line.startsWith("FinalState:")) {
                    String[] states = line.substring(11).split(",");
                    for (String state : states) {
                        finalStates.add(state.trim());
                    }
                    continue;
                }

                // Check for Transitions section
                if (line.equalsIgnoreCase("Transitions:")) {
                    isTransitionSection = true;
                    continue;
                }

                // Process transition lines
                if (isTransitionSection) {
                    String[] parts = line.split(" ");
                    if (parts.length == 5) {
                        String currentState = parts[0];
                        String readSymbol = parts[1];
                        String nextState = parts[2];
                        String writeSymbol = parts[3];
                        String direction = parts[4];

                        transitions.add(new Transition(currentState, readSymbol, writeSymbol, direction, nextState));
                    } else {
                        System.err.println("Invalid transition format: " + line);
                    }
                }
            }
        }
    }

    // Run the Turing machine with the given input string
    public boolean run(String input) {
        tape = new LinkedList<>();
        // Initialize tape with input and blank symbol ('B')
        for (char c : (input + BLANK).toCharArray()) {
            tape.add(c);
        }

        headPosition = 0;
        currentState = "q0";

        // Process the input tape and apply transitions
        while (true) {
            System.out.println("Tape: " + tape);
            System.out.println("Current State: " + currentState);
            System.out.println("Head Position: " + headPosition);

            // Check if the current state is a final state
            if (finalStates.contains(currentState)) {
                System.out.println("Machine reached accepting state: " + currentState);
                return true;
            }

            // Find the appropriate transition for the current state and symbol
            Transition transition = getTransition(currentState, String.valueOf(tape.get(headPosition)));
            if (transition == null) {
                System.out.println("No valid transition found. Machine halts.");
                return false;
            }

            // Apply the transition
            tape.set(headPosition, transition.writeSymbol.charAt(0));
            currentState = transition.nextState;

            // Move the head in the specified direction (Left or Right)
            headPosition = transition.direction.equals("R") ? headPosition + 1 : headPosition - 1;
            if (headPosition < 0) {
                System.out.println("Head moved out of bounds. Rejecting input.");
                return false;
            } else if (headPosition >= tape.size()) {
                tape.add(BLANK.charAt(0));
            }
        }
    }

    // Get the transition for the given state and symbol
    private Transition getTransition(String state, String symbol) {
        for (Transition t : transitions) {
            if (t.currentState.equals(state) && t.readSymbol.equals(symbol)) {
                return t;
            }
        }
        return null;
    }

    // Define the Transition class to hold information about state transitions
    private static class Transition {
        String currentState, readSymbol, writeSymbol, direction, nextState;
    
        Transition(String currentState, String readSymbol, String writeSymbol, String direction, String nextState) {
            this.currentState = currentState;
            this.readSymbol = readSymbol;
            this.writeSymbol = writeSymbol;
            this.direction = direction;
            this.nextState = nextState;
        }
    }
}
