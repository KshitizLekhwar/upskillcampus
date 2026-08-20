import java.io.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

// --- DATA MODELS ---

class Employee implements Serializable {
    private static final long serialVersionUID = 1L;
    private String id;
    private String name;
    private String designation;
    private String department;
    private String contact;

    public Employee(String id, String name, String designation, String department, String contact) {
        this.id = id;
        this.name = name;
        this.designation = designation;
        this.department = department;
        this.contact = contact;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDesignation() { return designation; }
    public void setDesignation(String designation) { this.designation = designation; }
    public String getDepartment() { return department; }
    public void setDepartment(String department) { this.department = department; }
    public String getContact() { return contact; }
    public void setContact(String contact) { this.contact = contact; }

    @Override
    public String toString() {
        return String.format("| %-10s | %-18s | %-18s | %-15s | %-15s |", 
                id, name, designation, department, contact);
    }
}

class AttendanceRecord implements Serializable {
    private static final long serialVersionUID = 1L;
    private String employeeId;
    private LocalDate date;
    private String status; // Present, Absent, On Leave

    public AttendanceRecord(String employeeId, LocalDate date, String status) {
        this.employeeId = employeeId;
        this.date = date;
        this.status = status;
    }

    public String getEmployeeId() { return employeeId; }
    public LocalDate getDate() { return date; }
    public String getStatus() { return status; }

    @Override
    public String toString() {
        return String.format("| %-10s | %-12s | %-12s |", employeeId, date, status);
    }
}

class LeaveRequest implements Serializable {
    private static final long serialVersionUID = 1L;
    private static int idCounter = 101;
    private int requestId;
    private String employeeId;
    private String leaveType;
    private LocalDate startDate;
    private LocalDate endDate;
    private String status; // PENDING, APPROVED, REJECTED

    public LeaveRequest(String employeeId, String leaveType, LocalDate startDate, LocalDate endDate) {
        this.requestId = idCounter++;
        this.employeeId = employeeId;
        this.leaveType = leaveType;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = "PENDING";
    }

    public int getRequestId() { return requestId; }
    public String getEmployeeId() { return employeeId; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return String.format("| %-6d | %-10s | %-12s | %-10s to %-10s | %-10s |", 
                requestId, employeeId, leaveType, startDate, endDate, status);
    }
}

// --- SYSTEM STATE CONTAINER ---

class HRMSData implements Serializable {
    private static final long serialVersionUID = 1L;
    Map<String, Employee> employees = new HashMap<>();
    List<AttendanceRecord> attendanceList = new ArrayList<>();
    List<LeaveRequest> leaveRequests = new ArrayList<>();
}

// --- MAIN APPLICATION CLASS ---

public class HumanResourceManagementSystem {
    private static final String DATA_FILE = "hrms_data.ser";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static HRMSData data = new HRMSData();
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        loadData();
        if (authenticate()) {
            boolean running = true;
            while (running) {
                printMainMenu();
                int choice = getIntInput("Enter your choice: ");
                switch (choice) {
                    case 1 -> employeeManagementMenu();
                    case 2 -> attendanceMenu();
                    case 3 -> leaveManagementMenu();
                    case 4 -> searchMenu();
                    case 5 -> generateReports();
                    case 6 -> {
                        saveData();
                        System.out.println("\n[+] Data saved successfully. Exiting system. Goodbye!");
                        running = false;
                    }
                    default -> System.out.println("[!] Invalid option. Please try again.");
                }
            }
        }
    }

    // --- AUTHENTICATION ---

    private static boolean authenticate() {
        System.out.println("==================================================");
        System.out.println("   HUMAN RESOURCE MANAGEMENT SYSTEM (HRMS)");
        System.out.println("==================================================");
        int attempts = 3;
        while (attempts > 0) {
            System.out.print("Enter Admin Username: ");
            String user = scanner.nextLine().trim();
            System.out.print("Enter Admin Password: ");
            String pass = scanner.nextLine().trim();

            if (user.equals("admin") && pass.equals("admin123")) {
                System.out.println("\n[+] Login successful! Welcome, Admin.\n");
                return true;
            } else {
                attempts--;
                System.out.println("[!] Invalid credentials. Attempts remaining: " + attempts);
            }
        }
        System.out.println("[!] Maximum login attempts exceeded. Exiting.");
        return false;
    }

    // --- MENUS ---

    private static void printMainMenu() {
        System.out.println("\n================ MAIN MENU ================");
        System.out.println("1. Employee Management");
        System.out.println("2. Attendance Tracking");
        System.out.println("3. Leave Management");
        System.out.println("4. Search Employee");
        System.out.println("5. Generate Reports");
        System.out.println("6. Save & Exit");
        System.out.println("===========================================");
    }

    // --- 1. EMPLOYEE MANAGEMENT ---

    private static void employeeManagementMenu() {
        System.out.println("\n--- EMPLOYEE MANAGEMENT ---");
        System.out.println("1. Add Employee");
        System.out.println("2. View All Employees");
        System.out.println("3. Update Employee");
        System.out.println("4. Delete Employee");
        System.out.println("5. Back to Main Menu");
        int choice = getIntInput("Choose an option: ");

        switch (choice) {
            case 1 -> {
                System.out.print("Enter Employee ID: ");
                String id = scanner.nextLine().trim();
                if (data.employees.containsKey(id)) {
                    System.out.println("[!] Error: Employee ID already exists!");
                    return;
                }
                System.out.print("Enter Full Name: ");
                String name = scanner.nextLine().trim();
                System.out.print("Enter Designation: ");
                String designation = scanner.nextLine().trim();
                System.out.print("Enter Department: ");
                String department = scanner.nextLine().trim();
                System.out.print("Enter Contact Number: ");
                String contact = scanner.nextLine().trim();

                data.employees.put(id, new Employee(id, name, designation, department, contact));
                System.out.println("[+] Confirmation: Employee added successfully!");
            }
            case 2 -> viewAllEmployees();
            case 3 -> {
                System.out.print("Enter Employee ID to update: ");
                String id = scanner.nextLine().trim();
                Employee emp = data.employees.get(id);
                if (emp == null) {
                    System.out.println("[!] Error: Employee not found.");
                    return;
                }
                System.out.print("Enter New Name (" + emp.getName() + "): ");
                String name = scanner.nextLine().trim();
                if (!name.isEmpty()) emp.setName(name);

                System.out.print("Enter New Designation (" + emp.getDesignation() + "): ");
                String desig = scanner.nextLine().trim();
                if (!desig.isEmpty()) emp.setDesignation(desig);

                System.out.print("Enter New Department (" + emp.getDepartment() + "): ");
                String dept = scanner.nextLine().trim();
                if (!dept.isEmpty()) emp.setDepartment(dept);

                System.out.print("Enter New Contact (" + emp.getContact() + "): ");
                String contact = scanner.nextLine().trim();
                if (!contact.isEmpty()) emp.setContact(contact);

                System.out.println("[+] Confirmation: Employee record updated successfully!");
            }
            case 4 -> {
                System.out.print("Enter Employee ID to delete: ");
                String id = scanner.nextLine().trim();
                if (data.employees.remove(id) != null) {
                    System.out.println("[+] Confirmation: Employee record deleted successfully!");
                } else {
                    System.out.println("[!] Error: Employee not found.");
                }
            }
            case 5 -> {}
            default -> System.out.println("[!] Invalid option.");
        }
    }

    private static void viewAllEmployees() {
        if (data.employees.isEmpty()) {
            System.out.println("[!] No employee records available.");
            return;
        }
        System.out.println("\n+------------+--------------------+--------------------+-----------------+-----------------+");
        System.out.println("| ID         | Name               | Designation        | Department      | Contact         |");
        System.out.println("+------------+--------------------+--------------------+-----------------+-----------------+");
        for (Employee emp : data.employees.values()) {
            System.out.println(emp);
        }
        System.out.println("+------------+--------------------+--------------------+-----------------+-----------------+");
    }

    // --- 2. ATTENDANCE TRACKING ---

    private static void attendanceMenu() {
        System.out.println("\n--- ATTENDANCE TRACKING ---");
        System.out.println("1. Mark Attendance");
        System.out.println("2. View Attendance by Date");
        System.out.println("3. View Attendance by Employee");
        System.out.println("4. Back to Main Menu");
        int choice = getIntInput("Choose an option: ");

        switch (choice) {
            case 1 -> {
                System.out.print("Enter Employee ID: ");
                String id = scanner.nextLine().trim();
                if (!data.employees.containsKey(id)) {
                    System.out.println("[!] Error: Employee ID not found.");
                    return;
                }
                LocalDate date = getDateInput("Enter Date (YYYY-MM-DD) or press Enter for today: ");
                System.out.println("Select Status: 1. Present  2. Absent  3. On Leave");
                int stChoice = getIntInput("Choice: ");
                String status = switch (stChoice) {
                    case 1 -> "Present";
                    case 2 -> "Absent";
                    case 3 -> "On Leave";
                    default -> "Present";
                };
                data.attendanceList.add(new AttendanceRecord(id, date, status));
                System.out.println("[+] Confirmation: Attendance recorded for " + id + " on " + date + " as " + status);
            }
            case 2 -> {
                LocalDate date = getDateInput("Enter Date (YYYY-MM-DD): ");
                System.out.println("\n+------------+--------------+--------------+");
                System.out.println("| Emp ID     | Date         | Status       |");
                System.out.println("+------------+--------------+--------------+");
                for (AttendanceRecord rec : data.attendanceList) {
                    if (rec.getDate().equals(date)) {
                        System.out.println(rec);
                    }
                }
                System.out.println("+------------+--------------+--------------+");
            }
            case 3 -> {
                System.out.print("Enter Employee ID: ");
                String id = scanner.nextLine().trim();
                System.out.println("\n+------------+--------------+--------------+");
                System.out.println("| Emp ID     | Date         | Status       |");
                System.out.println("+------------+--------------+--------------+");
                for (AttendanceRecord rec : data.attendanceList) {
                    if (rec.getEmployeeId().equalsIgnoreCase(id)) {
                        System.out.println(rec);
                    }
                }
                System.out.println("+------------+--------------+--------------+");
            }
            case 4 -> {}
            default -> System.out.println("[!] Invalid option.");
        }
    }

    // --- 3. LEAVE MANAGEMENT ---

    private static void leaveManagementMenu() {
        System.out.println("\n--- LEAVE MANAGEMENT ---");
        System.out.println("1. Submit Leave Request");
        System.out.println("2. View All Leave Requests");
        System.out.println("3. Approve / Reject Leave Request");
        System.out.println("4. Back to Main Menu");
        int choice = getIntInput("Choose an option: ");

        switch (choice) {
            case 1 -> {
                System.out.print("Enter Employee ID: ");
                String id = scanner.nextLine().trim();
                if (!data.employees.containsKey(id)) {
                    System.out.println("[!] Error: Employee ID not found.");
                    return;
                }
                System.out.print("Enter Leave Type (Sick, Casual, Paid): ");
                String type = scanner.nextLine().trim();
                LocalDate start = getDateInput("Enter Start Date (YYYY-MM-DD): ");
                LocalDate end = getDateInput("Enter End Date (YYYY-MM-DD): ");

                LeaveRequest req = new LeaveRequest(id, type, start, end);
                data.leaveRequests.add(req);
                System.out.println("[+] Confirmation: Leave request submitted. Request ID: " + req.getRequestId());
            }
            case 2 -> {
                if (data.leaveRequests.isEmpty()) {
                    System.out.println("[!] No leave requests found.");
                    return;
                }
                System.out.println("\n+--------+------------+--------------+-------------------------+------------+");
                System.out.println("| Req ID | Emp ID     | Leave Type   | Duration                | Status     |");
                System.out.println("+--------+------------+--------------+-------------------------+------------+");
                for (LeaveRequest req : data.leaveRequests) {
                    System.out.println(req);
                }
                System.out.println("+--------+------------+--------------+-------------------------+------------+");
            }
            case 3 -> {
                int reqId = getIntInput("Enter Leave Request ID: ");
                LeaveRequest target = null;
                for (LeaveRequest req : data.leaveRequests) {
                    if (req.getRequestId() == reqId) {
                        target = req;
                        break;
                    }
                }
                if (target == null) {
                    System.out.println("[!] Error: Request ID not found.");
                    return;
                }
                System.out.println("Select Action: 1. Approve  2. Reject");
                int action = getIntInput("Choice: ");
                if (action == 1) {
                    target.setStatus("APPROVED");
                    System.out.println("[+] Confirmation: Request " + reqId + " marked as APPROVED.");
                } else if (action == 2) {
                    target.setStatus("REJECTED");
                    System.out.println("[+] Confirmation: Request " + reqId + " marked as REJECTED.");
                } else {
                    System.out.println("[!] Invalid choice.");
                }
            }
            case 4 -> {}
            default -> System.out.println("[!] Invalid option.");
        }
    }

    // --- 4. EMPLOYEE SEARCH ---

    private static void searchMenu() {
        System.out.println("\n--- SEARCH EMPLOYEE ---");
        System.out.println("1. Search by ID");
        System.out.println("2. Search by Name");
        System.out.println("3. Search by Department");
        int choice = getIntInput("Choose an option: ");

        List<Employee> results = new ArrayList<>();
        switch (choice) {
            case 1 -> {
                System.out.print("Enter Employee ID: ");
                String id = scanner.nextLine().trim();
                Employee emp = data.employees.get(id);
                if (emp != null) results.add(emp);
            }
            case 2 -> {
                System.out.print("Enter Name: ");
                String name = scanner.nextLine().trim().toLowerCase();
                for (Employee emp : data.employees.values()) {
                    if (emp.getName().toLowerCase().contains(name)) results.add(emp);
                }
            }
            case 3 -> {
                System.out.print("Enter Department: ");
                String dept = scanner.nextLine().trim().toLowerCase();
                for (Employee emp : data.employees.values()) {
                    if (emp.getDepartment().toLowerCase().contains(dept)) results.add(emp);
                }
            }
            default -> System.out.println("[!] Invalid search criteria.");
        }

        if (results.isEmpty()) {
            System.out.println("[!] No matching records found.");
        } else {
            System.out.println("\n+------------+--------------------+--------------------+-----------------+-----------------+");
            System.out.println("| ID         | Name               | Designation        | Department      | Contact         |");
            System.out.println("+------------+--------------------+--------------------+-----------------+-----------------+");
            for (Employee emp : results) {
                System.out.println(emp);
            }
            System.out.println("+------------+--------------------+--------------------+-----------------+-----------------+");
        }
    }

    // --- 5. REPORT GENERATION ---

    private static void generateReports() {
        System.out.println("\n================ HRMS SUMMARY REPORT ================");
        System.out.println("Total Registered Employees: " + data.employees.size());
        System.out.println("Total Attendance Records  : " + data.attendanceList.size());
        System.out.println("Total Leave Applications  : " + data.leaveRequests.size());

        long approvedLeaves = data.leaveRequests.stream().filter(l -> "APPROVED".equals(l.getStatus())).count();
        long pendingLeaves = data.leaveRequests.stream().filter(l -> "PENDING".equals(l.getStatus())).count();

        System.out.println("Approved Leaves           : " + approvedLeaves);
        System.out.println("Pending Leaves            : " + pendingLeaves);
        System.out.println("=====================================================");
    }

    // --- PERSISTENCE ---

    private static void saveData() {
        try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(DATA_FILE))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.out.println("[!] Warning: Failed to save data to file: " + e.getMessage());
        }
    }

    private static void loadData() {
        File file = new File(DATA_FILE);
        if (!file.exists()) return;
        try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(DATA_FILE))) {
            data = (HRMSData) ois.readObject();
        } catch (Exception e) {
            System.out.println("[!] Warning: Could not read existing data. Initializing clean state.");
        }
    }

    // --- INPUT HELPERS ---

    private static int getIntInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("[!] Invalid number format. Try again.");
            }
        }
    }

    private static LocalDate getDateInput(String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (input.isEmpty()) return LocalDate.now();
            try {
                return LocalDate.parse(input, DATE_FORMAT);
            } catch (DateTimeParseException e) {
                System.out.println("[!] Invalid date format. Use YYYY-MM-DD.");
            }
        }
    }
}