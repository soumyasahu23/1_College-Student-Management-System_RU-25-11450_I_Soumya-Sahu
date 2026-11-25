import java.io.*;
import java.util.*;

/**
 * College Student Management System (PS No. 1)
 * Concepts: ArrayList, HashMap (duplicate check + delete lookup), Linear Search,
 *           Insertion Sort, Methods, File Handling
 */
class Student {
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

    String getId()      { return id; }
    String getName()    { return name; }
    String getCourse()  { return course; }
    double getCgpa()    { return cgpa; }

    // File line format: ID|Name|Course|CGPA
    String toFileLine() {
        return id + "|" + name + "|" + course + "|" + cgpa;
    }

    @Override
    public String toString() {
        return id + " | " + name + " | " + course + " | " + cgpa;
    }
}

public class Main {
    private static final String FILE_NAME = "students.txt";
    private static final int FIRST_ID = 101;

    // ArrayList keeps insertion order / used for display and sorting
    private static final ArrayList<Student> students = new ArrayList<>();
    // HashMap gives O(1) duplicate-ID check and quick lookup when deleting
    private static final HashMap<String, Student> idMap = new HashMap<>();

    private static int nextIdNumber = FIRST_ID;
    private static final Scanner sc = new Scanner(System.in);

    public static void main(String[] args) {
        loadFromFile();

        boolean running = true;
        while (running) {
            printMenu();
            String choice = sc.nextLine().trim();
            switch (choice) {
                case "1": addStudent();        break;
                case "2": displayAll();        break;
                case "3": searchById();        break;
                case "4": sortByCgpa();        break;
                case "5": deleteStudent();     break;
                case "6": searchByName();      break;
                case "7":
                    saveToFile();
                    System.out.println("Data saved. Goodbye!");
                    running = false;
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1-7.");
            }
        }
        sc.close();
    }

    // ---------------- Menu ----------------
    private static void printMenu() {
        System.out.println("\n===== College Student Management System =====");
        System.out.println("1. Add student");
        System.out.println("2. View all students");
        System.out.println("3. Search by ID");
        System.out.println("4. Sort by CGPA (high to low)");
        System.out.println("5. Delete student");
        System.out.println("6. Search by name");
        System.out.println("7. Save & Exit");
        System.out.print("Enter choice: ");
    }

    // ---------------- Features ----------------
    private static void addStudent() {
        System.out.print("Name: ");
        String name = sc.nextLine().trim();
        if (name.isEmpty()) {
            System.out.println("Name cannot be empty.");
            return;
        }
        System.out.print("Course: ");
        String course = sc.nextLine().trim();
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
        if (cgpa < 0 || cgpa > 10) {
            System.out.println("Invalid CGPA. It must be between 0 and 10.");
            return;
        }

        // Generate a unique ID (S101, S102, ...) and guard against duplicates
        String id = "S" + nextIdNumber;
        while (idMap.containsKey(id)) {
            nextIdNumber++;
            id = "S" + nextIdNumber;
        }
        nextIdNumber++;

        Student s = new Student(id, name, course, cgpa);
        students.add(s);
        idMap.put(id, s);
        System.out.println("Student added. ID: " + s);
        saveToFile();
    }

    private static void displayAll() {
        if (students.isEmpty()) {
            System.out.println("No students found.");
            return;
        }
        for (int i = 0; i < students.size(); i++) {
            System.out.println((i + 1) + ". " + students.get(i));
        }
    }

    // Linear search: check every student one by one - O(n)
    private static Student linearSearchById(String id) {
        for (int i = 0; i < students.size(); i++) {
            if (students.get(i).getId().equalsIgnoreCase(id)) {
                return students.get(i);
            }
        }
        return null;
    }

    private static void searchById() {
        System.out.print("Student ID: ");
        String id = sc.nextLine().trim().toUpperCase();
        Student s = linearSearchById(id);
        if (s == null) {
            System.out.println("Student ID " + id + " not found.");
        } else {
            System.out.println(s);
        }
    }

    // Linear search over the ArrayList (partial, case-insensitive match)
    private static void searchByName() {
        System.out.print("Name: ");
        String key = sc.nextLine().trim().toLowerCase();
        boolean found = false;
        for (Student s : students) {
            if (s.getName().toLowerCase().contains(key)) {
                System.out.println(s);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No student found with name \"" + key + "\".");
        }
    }

    // Insertion sort (manual) - CGPA high to low. Stable, O(n^2) worst case.
    private static void insertionSortByCgpa(ArrayList<Student> list) {
        for (int i = 1; i < list.size(); i++) {
            Student key = list.get(i);
            int j = i - 1;
            // shift students with LOWER cgpa one place to the right
            while (j >= 0 && list.get(j).getCgpa() < key.getCgpa()) {
                list.set(j + 1, list.get(j));
                j--;
            }
            list.set(j + 1, key);
        }
    }

    private static void sortByCgpa() {
        if (students.isEmpty()) {
            System.out.println("No students to sort.");
            return;
        }
        ArrayList<Student> sorted = new ArrayList<>(students); // copy, original order kept
        insertionSortByCgpa(sorted);

        System.out.println("Sorted list (" + sorted.size() + " student" + (sorted.size() == 1 ? "" : "s") + "):");
        for (int i = 0; i < sorted.size(); i++) {
            System.out.println((i + 1) + ". " + sorted.get(i));
        }
    }

    private static void deleteStudent() {
        System.out.print("Student ID: ");
        String id = sc.nextLine().trim().toUpperCase();
        Student s = idMap.remove(id);
        if (s == null) {
            System.out.println("Student ID " + id + " not found.");
            return;
        }
        students.remove(s);
        System.out.println("Student " + id + " deleted.");
        saveToFile();
    }

    // ---------------- File Handling ----------------
    private static void loadFromFile() {
        File file = new File(FILE_NAME);
        if (!file.exists()) {
            return; // first run: nothing to load
        }
        try (BufferedReader br = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = br.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) continue;
                String[] p = line.split("\\|");
                if (p.length != 4) continue;
                try {
                    String id = p[0].trim();
                    double cgpa = Double.parseDouble(p[3].trim());
                    if (cgpa < 0 || cgpa > 10 || idMap.containsKey(id)) continue; // skip bad/duplicate rows
                    Student s = new Student(id, p[1].trim(), p[2].trim(), cgpa);
                    students.add(s);
                    idMap.put(id, s);
                    if (id.length() > 1 && id.startsWith("S")) {
                        int num = Integer.parseInt(id.substring(1));
                        if (num >= nextIdNumber) nextIdNumber = num + 1;
                    }
                } catch (NumberFormatException ignored) {
                    // skip malformed line
                }
            }
        } catch (IOException e) {
            System.out.println("Could not read " + FILE_NAME + ": " + e.getMessage());
        }
    }

    private static void saveToFile() {
        try (PrintWriter pw = new PrintWriter(new FileWriter(FILE_NAME))) {
            for (Student s : students) {
                pw.println(s.toFileLine());
            }
        } catch (IOException e) {
            System.out.println("Could not save " + FILE_NAME + ": " + e.getMessage());
        }
    }
}