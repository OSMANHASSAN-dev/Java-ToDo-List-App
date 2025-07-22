import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a single task in the To-Do list.
 * Implements Serializable to allow saving and loading of Task objects to/from a file.
 */
class Task implements Serializable {
    private static final long serialVersionUID = 1L; // For serialization compatibility
    private String description;
    private boolean isCompleted;

    /**
     * Constructor for a new Task.
     * @param description The textual description of the task.
     */
    public Task(String description) {
        this.description = description;
        this.isCompleted = false; // Tasks are not completed by default
    }

    /**
     * Gets the description of the task.
     * @return The task description.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the task.
     * @param description The new task description.
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Checks if the task is completed.
     * @return True if the task is completed, false otherwise.
     */
    public boolean isCompleted() {
        return isCompleted;
    }

    /**
     * Sets the completion status of the task.
     * @param completed True to mark as completed, false to mark as not completed.
     */
    public void setCompleted(boolean completed) {
        isCompleted = completed;
    }

    /**
     * Overrides the toString method to provide a formatted string representation
     * for display in the JList.
     * @return A string indicating completion status and description.
     */
    @Override
    public String toString() {
        return (isCompleted ? "[DONE] " : "[TODO] ") + description;
    }
}

/**
 * Main application class for the To-Do List.
 * Extends JFrame to create the main window of the GUI.
 */
public class TodoListApp extends JFrame {

    private static final String DATA_FILE = "tasks.ser"; // File to save/load tasks
    private DefaultListModel<Task> taskListModel; // Model for the JList
    private JList<Task> taskList; // Displays the list of tasks
    private JTextField taskInputField; // Input field for new tasks

    /**
     * Constructor for the TodoListApp.
     * Sets up the GUI and loads existing tasks.
     */
    public TodoListApp() {
        // --- Frame Setup ---
        setTitle("Advanced To-Do List App");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 450); // Increased size for better layout
        setLocationRelativeTo(null); // Center the window on the screen
        setResizable(true); // Allow resizing

        // Set a modern look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            System.err.println("Could not set system look and feel: " + e.getMessage());
        }

        // --- Main Panel (Content Pane) ---
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout(10, 10)); // Add gaps between components
        mainPanel.setBorder(new EmptyBorder(15, 15, 15, 15)); // Add padding around the edges
        add(mainPanel);

        // --- Task Input Panel ---
        JPanel inputPanel = new JPanel(new BorderLayout(5, 5));
        taskInputField = new JTextField(30);
        taskInputField.setFont(new Font("Segoe UI", Font.PLAIN, 16)); // Modern font
        taskInputField.putClientProperty("JTextField.placeholderText", "Enter a new task here..."); // Placeholder text

        JButton addTaskButton = new JButton("Add Task");
        addTaskButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        addTaskButton.setBackground(new Color(60, 179, 113)); // MediumSeaGreen
        addTaskButton.setForeground(Color.BLACK); // Changed to BLACK
        addTaskButton.setFocusPainted(false); // Remove focus border

        inputPanel.add(taskInputField, BorderLayout.CENTER);
        inputPanel.add(addTaskButton, BorderLayout.EAST);
        mainPanel.add(inputPanel, BorderLayout.NORTH);

        // --- Task List Display ---
        taskListModel = new DefaultListModel<>();
        taskList = new JList<>(taskListModel);
        taskList.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        taskList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION); // Only one task can be selected at a time

        // Custom renderer to strike through completed tasks
        taskList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                          boolean isSelected, boolean cellHasFocus) {
                JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                Task task = (Task) value;
                if (task.isCompleted()) {
                    // HTML for strikethrough effect
                    label.setText("<html><strike>" + task.getDescription() + "</strike></html>");
                    label.setForeground(Color.GRAY); // Gray out completed tasks
                } else {
                    label.setText(task.getDescription());
                    label.setForeground(Color.BLACK);
                }
                // Add a prefix to show status explicitly
                label.setText((task.isCompleted() ? "✓ " : "○ ") + label.getText());
                return label;
            }
        });

        JScrollPane scrollPane = new JScrollPane(taskList);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1)); // Light gray border
        mainPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Action Buttons Panel ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10)); // Centered flow layout
        buttonPanel.setBorder(new EmptyBorder(5, 0, 0, 0)); // Top padding

        JButton markCompleteButton = new JButton("Mark Complete");
        markCompleteButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        markCompleteButton.setBackground(new Color(30, 144, 255)); // DodgerBlue
        markCompleteButton.setForeground(Color.BLACK); // Changed to BLACK
        markCompleteButton.setFocusPainted(false);

        JButton deleteButton = new JButton("Delete Task");
        deleteButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        deleteButton.setBackground(new Color(220, 20, 60)); // Crimson
        deleteButton.setForeground(Color.BLACK); // Changed to BLACK
        deleteButton.setFocusPainted(false);

        JButton editButton = new JButton("Edit Task");
        editButton.setFont(new Font("Segoe UI", Font.BOLD, 14));
        editButton.setBackground(new Color(255, 165, 0)); // Orange
        editButton.setForeground(Color.BLACK); // Changed to BLACK
        editButton.setFocusPainted(false);

        buttonPanel.add(addTaskButton); // Add button is also in input panel, but can be added here too for consistency
        buttonPanel.add(markCompleteButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        // --- Event Listeners ---
        addTaskButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });

        // Allow adding task by pressing Enter in the text field
        taskInputField.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                addTask();
            }
        });

        markCompleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                markTaskComplete();
            }
        });

        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                deleteTask();
            }
        });

        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                editTask();
            }
        });

        // Load tasks when the application starts
        loadTasks();
    }

    /**
     * Adds a new task to the list based on the input field's text.
     */
    private void addTask() {
        String taskDescription = taskInputField.getText().trim();
        if (!taskDescription.isEmpty()) {
            taskListModel.addElement(new Task(taskDescription));
            taskInputField.setText(""); // Clear the input field
            saveTasks(); // Save tasks after adding
        } else {
            JOptionPane.showMessageDialog(this, "Task description cannot be empty.",
                    "Input Error", JOptionPane.WARNING_MESSAGE);
        }
    }

    /**
     * Marks the selected task as complete or incomplete.
     */
    private void markTaskComplete() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            Task task = taskListModel.getElementAt(selectedIndex);
            task.setCompleted(!task.isCompleted()); // Toggle completion status
            // This line is crucial to refresh the JList display after changing task status
            taskListModel.setElementAt(task, selectedIndex);
            saveTasks(); // Save tasks after modification
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to mark complete/incomplete.",
                    "No Task Selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Edits the description of the selected task.
     */
    private void editTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            Task task = taskListModel.getElementAt(selectedIndex);
            String currentDescription = task.getDescription();
            String newDescription = JOptionPane.showInputDialog(this,
                    "Edit task description:", "Edit Task",
                    JOptionPane.PLAIN_MESSAGE, null, null, currentDescription).toString();

            if (newDescription != null && !newDescription.trim().isEmpty()) {
                task.setDescription(newDescription.trim());
                // This line is crucial to refresh the JList display after changing task description
                taskListModel.setElementAt(task, selectedIndex);
                saveTasks(); // Save tasks after modification
            } else if (newDescription != null) { // User entered empty string
                JOptionPane.showMessageDialog(this, "Task description cannot be empty.",
                        "Input Error", JOptionPane.WARNING_MESSAGE);
            }
            // If newDescription is null, user cancelled the dialog, do nothing.
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to edit.",
                    "No Task Selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Deletes the selected task from the list.
     */
    private void deleteTask() {
        int selectedIndex = taskList.getSelectedIndex();
        if (selectedIndex != -1) {
            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete this task?", "Confirm Deletion",
                    JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
            if (confirm == JOptionPane.YES_OPTION) {
                taskListModel.remove(selectedIndex);
                saveTasks(); // Save tasks after deletion
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a task to delete.",
                    "No Task Selected", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * Saves the current list of tasks to a file using serialization.
     */
    private void saveTasks() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            List<Task> tasksToSave = new ArrayList<>();
            for (int i = 0; i < taskListModel.size(); i++) {
                tasksToSave.add(taskListModel.getElementAt(i));
            }
            oos.writeObject(tasksToSave);
            System.out.println("Tasks saved successfully.");
        } catch (IOException e) {
            System.err.println("Error saving tasks: " + e.getMessage());
            JOptionPane.showMessageDialog(this, "Error saving tasks: " + e.getMessage(),
                    "Save Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    /**
     * Loads tasks from a file using deserialization.
     * If the file does not exist, a new empty list is used.
     */
    private void loadTasks() {
        File file = new File(DATA_FILE);
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
                @SuppressWarnings("unchecked") // Suppress warning for unchecked cast
                List<Task> loadedTasks = (List<Task>) ois.readObject();
                taskListModel.clear(); // Clear existing tasks before loading
                for (Task task : loadedTasks) {
                    taskListModel.addElement(task);
                }
                System.out.println("Tasks loaded successfully.");
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading tasks: " + e.getMessage());
                JOptionPane.showMessageDialog(this, "Error loading tasks: " + e.getMessage(),
                        "Load Error", JOptionPane.ERROR_MESSAGE);
            }
        } else {
            System.out.println("No existing task file found. Starting with an empty list.");
        }
    }

    /**
     * Main method to run the To-Do List application.
     * Creates and displays the JFrame on the Event Dispatch Thread (EDT).
     * @param args Command line arguments (not used).
     */
    public static void main(String[] args) {
        // Ensure GUI updates are done on the Event Dispatch Thread
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new TodoListApp().setVisible(true);
            }
        });
    }
}
