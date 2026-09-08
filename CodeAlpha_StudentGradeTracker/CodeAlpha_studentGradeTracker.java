import java.util.*;
public class CodeAlpha_studentGradeTracker {
    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        // creating ArrayList for Student name and Marks.
        ArrayList<String> studentName = new ArrayList<>();
        ArrayList<Double> studentMarks = new ArrayList<>();

        // Taking input of Number of Students
        int n;
        while(true){
            System.out.print("Enter number of Students:");
            n = sc.nextInt();
            System.out.println();
            if(n>0){
                break;
            }
            System.out.println("Number of students must be gerater than 0.\n");
        }

        // Adding Student Name and Their marks in ArrayList
        for(int i = 1; i<=n; i++){
            System.out.print("Enter "+i+" Student name: ");
            String name = sc.next();
            studentName.add(name);
            System.out.print("Enter "+name+" marks: ");
            double marks = sc.nextDouble();
            studentMarks.add(marks);
            System.out.println();
        }

        double total = 0;
        double highest = studentMarks.get(0);
        double lowest = studentMarks.get(0);

        // Calculating Highest and Lowest Marks
        for(int i = 0; i<n; i++){
            total+= studentMarks.get(i);
            highest = Math.max(highest, studentMarks.get(i));
            lowest = Math.min(lowest, studentMarks.get(i));
        }

        // Calculating Average Marks
        double average = total/n;

        // Display Student Summary
        System.out.println("-----Student Summary-----");
        System.out.println();
        for(int i = 0; i< n; i++){
            System.out.println(studentName.get(i)+": "+studentMarks.get(i));
        }
        System.out.println("---------------------");
        System.out.println("Average: "+average);
        System.out.println("Highest: "+highest);
        System.out.println("Lowest: "+lowest);
    }
}
