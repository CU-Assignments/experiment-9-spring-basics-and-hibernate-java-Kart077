EASY: 

File structure:

src/
 ├── model/
 │    ├── Course.java
 │    └── Student.java
 ├── config/
 │    └── AppConfig.java
 └── MainApp.java


Course.java:

package model;

public class Course {
    private String courseName;
    private int duration;

    public Course(String courseName, int duration) {
        this.courseName = courseName;
        this.duration = duration;
    }

    public String getCourseName() { return courseName; }
    public int getDuration() { return duration; }
}

Student.java:

package model;

public class Student {
    private String name;
    private Course course;

    public Student(String name, Course course) {
        this.name = name;
        this.course = course;
    }

    public void printDetails() {
        System.out.println("Student: " + name);
        System.out.println("Course: " + course.getCourseName() + ", Duration: " + course.getDuration());
    }
}

AppConfig.java:

package config;

import model.Course;
import model.Student;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {

    @Bean
    public Course course() {
        return new Course("Spring Framework", 40);
    }

    @Bean
    public Student student() {
        return new Student("Alice", course());
    }
}

Main.java:

import config.AppConfig;
import model.Student;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

public class MainApp {
    public static void main(String[] args) {
        ApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);
        Student student = context.getBean(Student.class);
        student.printDetails();
    }
}





MEDIUM:

Hibernate.cfg.xml:

<hibernate-configuration>
 <session-factory>
   <property name="hibernate.connection.driver_class">com.mysql.cj.jdbc.Driver</property>
   <property name="hibernate.connection.url">jdbc:mysql://localhost:3306/testdb</property>
   <property name="hibernate.connection.username">root</property>
   <property name="hibernate.connection.password">password</property>
   <property name="hibernate.dialect">org.hibernate.dialect.MySQL8Dialect</property>
   <property name="hibernate.hbm2ddl.auto">update</property>
   <mapping class="model.Student"/>
 </session-factory>
</hibernate-configuration>

Student.java:

package model;

import jakarta.persistence.*;

@Entity
@Table(name = "students")
public class Student {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String name;
    private int age;

    public Student() {}
    public Student(String name, int age) {
        this.name = name;
        this.age = age;
    }

    // Getters/Setters
}

HibernateUtil.java:

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = new Configuration().configure().buildSessionFactory();
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
}

StudentDAO.java:

import model.Student;
import org.hibernate.Session;
import org.hibernate.Transaction;

public class StudentDAO {
    public void create(Student s) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = session.beginTransaction();
        session.save(s);
        tx.commit();
        session.close();
    }

    // Add read, update, delete similarly
}

MainApp.java:

public class MainApp {
    public static void main(String[] args) {
        StudentDAO dao = new StudentDAO();
        Student s = new Student("Bob", 22);
        dao.create(s);
    }
}




HARD:

Account.java:

@Entity
public class Account {
    @Id
    private int id;
    private String name;
    private double balance;

    // Constructors, getters, setters
}

Transaction.java:

@Entity
public class TransactionRecord {
    @Id
    @GeneratedValue
    private int id;
    private int fromAccountId;
    private int toAccountId;
    private double amount;

    // Constructors, getters, setters
}

AppConfig.java:

@Configuration
@EnableTransactionManagement
@ComponentScan(basePackages = "your.package")
@PropertySource("classpath:application.properties")
public class AppConfig {

    @Bean
    public DataSource dataSource() { /* MySQL DataSource */ }

    @Bean
    public LocalSessionFactoryBean sessionFactory() {
        // set Hibernate properties and annotated classes
    }

    @Bean
    public HibernateTransactionManager transactionManager(SessionFactory sf) {
        return new HibernateTransactionManager(sf);
    }
}

BankService.java:

@Service
public class BankService {
    @Autowired
    private SessionFactory sessionFactory;

    @Transactional
    public void transfer(int fromId, int toId, double amount) {
        Session session = sessionFactory.getCurrentSession();
        Account from = session.get(Account.class, fromId);
        Account to = session.get(Account.class, toId);

        if (from.getBalance() < amount) throw new RuntimeException("Insufficient funds!");

        from.setBalance(from.getBalance() - amount);
        to.setBalance(to.getBalance() + amount);

        session.save(new TransactionRecord(fromId, toId, amount));
    }
}

MainApp.java:

public class MainApp {
    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        BankService service = ctx.getBean(BankService.class);

        try {
            service.transfer(1, 2, 1000);
            System.out.println("Transaction Successful");
        } catch (Exception e) {
            System.out.println("Transaction Failed: " + e.getMessage());
        }
    }
}
