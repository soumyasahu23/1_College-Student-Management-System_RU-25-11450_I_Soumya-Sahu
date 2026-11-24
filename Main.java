import java.io.*;
import java.util.*;

public class Main {

    static class Student {
        private final String id;
        private String name;
        private String course;
        private double cgpa;

        Student(String id, String name, String course, double cgpa) {
            this.id = id;
            this.name = name;
            this.course = course;
            this.cgpa = cgpa;
        }

        String getId()     { return id; }
        String getName()   { return name; }
        String getCourse() { return course; }
        double getCgpa()   { return cgpa; }

        @Override
        public String toString() {
            return id + " | " + name + " | " + course + " | " + cgpa;
        }

        String toFileLine() {
            return id + "|" + name + "|" + course + "|" + cgpa;
        }
    }

    //Data
    private static final String FILE_NAME = "students.txt";
    private static final ArrayList<Student> students = new ArrayList<>();
    private static final HashMap<String, Student> studentMap = new HashMap<>();
    private static final Scanner sc = new Scanner(System.in);

    //File handling
    static void loadFromFile() {
        File f = new File(FILE_NAME);
        if (!f.exists()) return;
        try (BufferedReader br = new BufferedReader(new FileReader(f))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length != 4) continue;
                try {
                    String id = p[0].trim();
                    double cgpa = Double.parseDouble(p[3].trim());
                    if (studentMap.containsKey(id) || cgpa < 0 || cgpa > 10) continue;
                    Student s = new Student(id, p[1].trim(), p[2].trim(), cgpa);
                    students.add(s);
                    studentMap.put(id, s);
                } catch (NumberFormatException ignored) {
                    // skip corrupted line
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    static void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Student s : students) {
                pw.println(s.toFileLine());
            }
        } catch (IOException e) {
            System.out.println("Error saving file: " + e.getMessage());
        }
    }

    //ID generation (first ID is S101)
    static String generateId() {
        int max = 100;
        for (Student s : students) {
            try {
                int n = Integer.parseInt(s.getId().substring(1));
                if (n > max) max = n;
            } catch (NumberFormatException ignored) { }
        }
        return "S" + (max + 1);
    }

    static void addStudent() {
        System.out.print("Name: ");
        String name = sc.nextLine().trim().replace("|", " ");
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        System.out.print("Course: ");
        String course = sc.nextLine().trim().replace("|", " ");
        if (course.isEmpty()) {
            System.out.println("Course cannot be empty.");
            return;
        }
        System.out.print("CGPA: ");
        double cgpa;
        try {
            cgpa = Double.parseDouble(sc.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("Invalid CGPA. Please enter a number between 0 and 10.");
            return;
        }
        if (Double.isNaN(cgpa) || cgpa < 0 || cgpa > 10) {
            System.out.println("Invalid CGPA. It must be between 0 and 10.");
            return;
        }

        String id = generateId();
        if (studentMap.containsKey(id)) {          // duplicate ID guard
            System.out.println("Duplicate ID not allowed.");
            return;
        }
        Student s = new Student(id, name, course, cgpa);
        students.add(s);
        studentMap.put(id, s);
        saveToFile();
        System.out.println("Student added. " + s);
    }

    static void displayAll() {
        if (students.isEmpty()) {
            System.out.println("No students found.");
            return;
        }
        System.out.println("All students (" + students.size() + "):");
        for (Student s : students) {
            System.out.println(s);
        }
    }

    static void searchStudent() {
        System.out.println("Search by: 1. ID  2. Name");
        System.out.print("Choice: ");
        String ch = sc.nextLine().trim();
        if (ch.equals("1")) {
            System.out.print("Student ID: ");
            String id = sc.nextLine().trim().toUpperCase();
            Student s = studentMap.get(id);           // fast HashMap lookup
            if (s == null) System.out.println("Student ID " + id + " not found.");
            else System.out.println(s);
        } else if (ch.equals("2")) {
            System.out.print("Name (full or partial): ");
            String q = sc.nextLine().trim().toLowerCase();
            boolean found = false;
            for (Student s : students) {              // linear search
                if (s.getName().toLowerCase().contains(q)) {
                    System.out.println(s);
                    found = true;
                }
            }
            if (!found) System.out.println("No student found with name \"" + q + "\".");
        } else {
            System.out.println("Invalid choice.");
        }
    }

    static void sortByCgpa() {
        if (students.isEmpty()) {
            System.out.println("No students to sort.");
            return;
        }
        ArrayList<Student> sorted = new ArrayList<>(students);
        sorted.sort(Comparator.comparingDouble(Student::getCgpa).reversed());   // high to low
        System.out.println("Sorted list (" + sorted.size() + " student" + (sorted.size() > 1 ? "s" : "") + "):");
        int i = 1;
        for (Student s : sorted) {
            System.out.println(i++ + ". " + s);
        }
    }

    static void deleteStudent() {
        System.out.print("Student ID: ");
        String id = sc.nextLine().trim().toUpperCase();
        Student s = studentMap.remove(id);
        if (s == null) {
            System.out.println("Student ID " + id + " not found.");
            return;
        }
        students.remove(s);
        saveToFile();
        System.out.println("Student " + id + " deleted.");
    }

    static void printMenu() {
        System.out.println("\n===== College Student Management System =====");
        System.out.println("1. Add student");
        System.out.println("2. View all students");
        System.out.println("3. Search student");
        System.out.println("4. Sort by CGPA");
        System.out.println("5. Delete student");
        System.out.println("6. Exit");
        System.out.print("Enter option: ");
    }

    public static void main(String[] args) {
        loadFromFile();
        boolean running = true;
        while (running) {
            printMenu();
            if (!sc.hasNextLine()) break;
            String opt = sc.nextLine().trim();
            switch (opt) {
                case "1": addStudent();    break;
                case "2": displayAll();    break;
                case "3": searchStudent(); break;
                case "4": sortByCgpa();    break;
                case "5": deleteStudent(); break;
                case "6": running = false; break;
                default:  System.out.println("Invalid option. Please choose 1-6.");
            }
        }
        saveToFile();
        System.out.println("Data saved to " + FILE_NAME + ". Goodbye!");
    }
}