# AUSMUN Management System

> A centralized, relationally sound Model United Nations (MUN) administrative platform designed to manage delegate registrations, country representations, committee assignments, session attendance, position paper evaluations, chair gradings, and awards distribution[cite: 1].

---

## Overview & Architecture

Model United Nations conferences require significant logistical coordination across multiple committees, producing interconnected administrative data streams[cite: 1]. Disconnected spreadsheets and manual tracking create cross-referencing bottlenecks, redundant entry, and high vulnerability to data inconsistency[cite: 1]. The AUSMUN Management System solves these problems by providing an enterprise-grade Oracle relational database coupled with a responsive Java Swing desktop interface via Java Database Connectivity (JDBC)[cite: 1].

```
                     +---------------------------------------+
                     |         Java Swing Desktop GUI        |
                     |  (MainFrame.java / NetBeans GUI Form) |
                     +---------------------------------------+
                                         |
                                         | JDBC Driver (ojdbc8)
                                         v
                     +---------------------------------------+
                     |        Oracle Database Schema         |
                     |   (14 Relational Tables + 1 View)     |
                     +---------------------------------------+
                                         |
       +-------------------+-------------+-------------+-------------------+
       |                   |                           |                   |
       v                   v                           v                   v
+--------------+  +-------------------+      +-------------------+  +---------------+
| Core Entities|  | Committee Domain  |      | Academic Records  |  | Event Ops     |
| - country    |  | - committee       |      | - position_paper  |  | - workshop    |
| - delegate   |  | - topic           |      | - evaluations     |  | - mun_session |
|              |  | - chair           |      | - feedback        |  | - attendance  |
|              |  | - awards          |      | - evaluation_scores| | - workshop_   |
|              |  | - award_recipients|      |   (Derived View)  |  |   attendees   |
+--------------+  +-------------------+      +-------------------+  +---------------+

```

### Development Lifecycle

1. **Problem Definition & Inception**: The platform targets the operational challenges of hosting MUN conferences at the American University of Sharjah (AUS), unifying delegate records, country rosters, chair assignments, and grading metrics into a single source of truth[cite: 1].
2. **Conceptual Modeling (ER Design)**:
* Formulated an Entity-Relationship (ER) model featuring 8 strong entities (`Country`, `Delegate`, `Committee`, `Chair`, `mun_session`, `Position_Paper`, `Awards`, `Workshop`) and 1 weak entity (`Attendance`)[cite: 1].
* Addressed multi-party evaluation dynamics using a specialized ternary relationship (`Evaluates`) connecting `Chair`, `Delegate`, and `Committee` to guarantee chairs evaluate delegates strictly within their assigned committee[cite: 1].
* Incorporated composite attributes (`f_name`, `l_name` for both delegates and chairs), multivalued attributes (`Topic` for committees, `Feedback` for chair evaluations), and derived attributes (Delegate `Age` calculated from date of birth; `EvaluationScore` derived from multi-criteria feedback)[cite: 1].


3. **Logical Relational Mapping**:
* Strong entities mapped into independent base relations[cite: 1].
* The weak entity `Attendance` mapped with composite primary key `(d_id, session_id)` inheriting foreign keys from `Delegate` and `mun_session`[cite: 1].
* 1:1 relationship `Delegate SUBMITS Position_Paper` mapped by placing `d_id` as a unique foreign key in `Position_Paper`[cite: 1].
* 1:N relationships (`Country REPRESENTS Delegate`, `Committee ASSIGNED TO Delegate`, `Committee SUPERVISED BY Chair`, `Committee GIVES Awards`, `Committee WRITTEN FOR Position_Paper`) mapped by placing parent primary keys as foreign keys on the N-side[cite: 1].
* M:N relationships mapped into bridge relations: `Delegate CAN ATTEND Workshop` mapped to `workshop_attendees (d_id, workshop_id)`, and `Delegate RECEIVES Awards` mapped to `award_recipients (d_id, aw_id)`[cite: 1].
* Multivalued attributes mapped into distinct tables: `topic (topic, com_id)` and composite multivalued `feedback (d_id, chair_id, com_id, professionalism, research, p_speaking)`[cite: 1].


4. **Physical Implementation & Hardening**:
* Implemented in Oracle SQL DDL across 14 tables[cite: 1]. Renamed `Session` to `mun_session` to avoid conflict with the Oracle reserved keyword[cite: 1].
* Added composite candidate keys and foreign keys (`delegate_did_com_uk` and `chair_id_com_uk`) to guarantee that position papers and ternary evaluations enforce identical committee assignments between delegates and chairs[cite: 1].
* Created the derived view `evaluation_scores` to calculate average performance scores per delegate across evaluation components dynamically[cite: 1, 2].


5. **Frontend Integration & Client-Side Validation**:
* Engineered an administrative dashboard in Java Swing using Apache NetBeans[cite: 1].
* Implemented client-side regular expressions, date parsing routines, and numeric range bounds before SQL transactions are dispatched to prevent database exception overhead[cite: 1].



---

## Tech Stack / Dependencies

* **Language**: Java 17 (or Java 11+)[cite: 3].
* **Database Management System**: Oracle Database 19c / 21c (Enterprise or Express Edition)[cite: 1].
* **Build System & Dependency Management**: Apache Maven 3.8+[cite: 3].
* **Database Driver**: Oracle JDBC Driver (`com.oracle.database.jdbc:ojdbc8:19.3.0.0` or newer).
* **GUI Framework**: Java Swing / AWT[cite: 1, 3].
* **Development Environment**: Apache NetBeans IDE / IntelliJ IDEA / Oracle SQL Developer[cite: 1, 3].

---

## Prerequisites & Environment Setup

### System Requirements

* **Java Development Kit (JDK)**: Version 17.0.x or higher installed and configured on the system path.
* **Apache Maven**: Version 3.8.x or higher installed.
* **Oracle Database**: An accessible Oracle instance (Oracle XE, Oracle 19c/21c, or Oracle Autonomous Database)[cite: 1].
* **Oracle SQL Developer / SQL*Plus**: Client tooling to execute schema DDL scripts[cite: 1].

### Environment Variables

Configure the following environment variables or system properties for the application runtime:

```bash
export DB_URL="jdbc:oracle:thin:@localhost:1521:xe"
export DB_USER="ausmun_admin"
export DB_PASSWORD="YourSecurePasswordHere"

```

---

## Installation & Build Instructions

### 1. Clone the Repository

```bash
git clone https://github.com/your-username/ausmun-management-system.git
cd ausmun-management-system

```

### 2. Database Provisioning in Oracle SQL

Execute the database definition script using Oracle SQL Developer or the `sqlplus` CLI client[cite: 1]. Ensure tables are created in order of foreign key dependency[cite: 1]:

```bash
sqlplus ausmun_admin/YourSecurePasswordHere@localhost:1521/xe @sql/AUSMUN_CMP3201.sql

```

The script performs the following sequentially:

* Generates all 14 physical tables (`country`, `committee`, `workshop`, `mun_session`, `delegate`, `chair`, `awards`, `position_paper`, `attendance`, `workshop_attendees`, `award_recipients`, `topic`, `evaluations`, `feedback`)[cite: 2].
* Enforces domain constraints (`veto_power`, `regi_status`, `score`, `attendance.status`, and feedback score bounds `[0, 10]`)[cite: 1, 2].
* Generates the derived calculation view `evaluation_scores`[cite: 1, 2].
* Seeds mock conference data across all relations and runs verification queries[cite: 1, 2].

### 3. Configure JDBC Connection

Open `MUNDatabaseGUI/src/main/java/edu/aus/mundatabase/MainFrame.java` (or your central connection manager) and update the connection parameters to match your local Oracle instance[cite: 1, 3]:

```java
String url = System.getenv("DB_URL") != null ? 
             System.getenv("DB_URL") : "jdbc:oracle:thin:@localhost:1521:xe";
String user = System.getenv("DB_USER") != null ? 
              System.getenv("DB_USER") : "ausmun_admin";
String password = System.getenv("DB_PASSWORD") != null ? 
                  System.getenv("DB_PASSWORD") : "YourSecurePasswordHere";

Connection conn = DriverManager.getConnection(url, user, password);

```

### 4. Build the GUI Application

Compile the project and bundle executable binaries using Maven[cite: 3]:

```bash
cd MUNDatabaseGUI
mvn clean compile
mvn package

```

This compiles all classes and produces `MUNDatabaseGUI-1.0-SNAPSHOT.jar` inside the `target/` directory[cite: 3].

---

## Usage & CLI/API Reference

### Launching the Application

Run the compiled JAR package directly from the command line:

```bash
java -jar target/MUNDatabaseGUI-1.0-SNAPSHOT.jar

```

Or execute directly through Maven:

```bash
mvn exec:java -Dexec.mainClass="edu.aus.mundatabase.MUNDatabaseGUI"

```

---

### GUI Interface & Operational Workflow

The application interface is structured across five primary tabs[cite: 1]:

#### 1. Committees Management

* **Fields**: Committee ID, Committee Name, Committee Type, Building Name, Room Number[cite: 1].
* **Features**: Create new committees, update room assignments, delete committees, and search by ID or Name[cite: 1].
* **Constraint Enforcement**: `com_name` must be unique across the conference[cite: 1].

#### 2. Delegates Management

* **Fields**: Delegate ID, First Name, Last Name, Email, School, Date of Birth, Registration Status, Country, Committee[cite: 1].
* **Validation**:
* Email must match RFC regex `^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$`[cite: 1].
* Date of Birth validated against `dd-MMM-yyyy` format[cite: 1].
* Country and Committee fields populated via lookup dropdowns (`loadCountries()`, `loadCommittees()`)[cite: 1].



#### 3. Attendance Tracking

* **Fields**: Delegate dropdown, Session dropdown, Status dropdown (`present`, `present_and_voting`, `absent`, `late`)[cite: 1].
* **Features**: Record presence per session, update status flags, search specific session attendances, and delete erroneous entries with confirmation prompts[cite: 1].

#### 4. Position Papers Management

* **Fields**: Paper Number, Submission Date, Content, Score, Delegate selector, Committee selector[cite: 1].
* **Validation**: Score bounded strictly between `0` and `100`[cite: 1]. Foreign keys verified so that papers must correspond to the delegate's assigned committee[cite: 1].

#### 5. Reports & Analytical Queries

Execute pre-compiled relational queries and display real-time results in interactive Swing tables[cite: 1]:

* **All Delegates**: Full delegate roster with assigned country and committee[cite: 1].
* **Delegates Per Committee**: Delegate headcount grouped by committee[cite: 1].
* **Committee Topics**: Multi-topic mappings across active bodies[cite: 1].
* **Attendance Details & Summaries**: Breakdown of present, late, and absent records per participant[cite: 1].
* **Evaluation Scores**: Derived scores generated dynamically via the `evaluation_scores` database view[cite: 1].
* **Award Recipients**: Correlated award titles with recipients and granting committees[cite: 1].
* **Position Paper Scores**: Ranked list of submitted papers and marks[cite: 1].

---

### Backend SQL Reference & Core Verification Queries

The backend schema supports direct relational queries via SQL*Plus or SQL Developer. The following queries represent core verification procedures:

#### Query: Derived Delegate Evaluation Scores

Calculates the derived evaluation score by averaging `professionalism`, `research`, and `p_speaking` across chairs[cite: 1, 2]:

```sql
SELECT
    d.f_name || ' ' || d.l_name AS delegate_name,
    ch.f_name || ' ' || ch.l_name AS chair_name,
    cm.com_name,
    es.score_ev
FROM evaluation_scores es
JOIN delegate d ON es.d_id = d.d_id
JOIN chair ch ON es.chair_id = ch.chair_id
JOIN committee cm ON es.com_id = cm.com_id
ORDER BY es.score_ev DESC;

```

**Expected Output:**

```text
DELEGATE_NAME    CHAIR_NAME      COM_NAME  SCORE_EV
---------------- ---------------- --------- --------
Sara Khan        Mariam Saeed    WHO           9.33
Renad Hamad      Reema Shubair   UNSC          8.67
Ahmed Mohsen     Youssef Hassan  ECOSOC        8.67
Ahmed Mohsen     Ranim Ali       ECOSOC        8.67
Fedaa Elmahdi    Youssef Hassan  ECOSOC        7.67
Daniel Zakhari   Shamma Almualla UNSC          7.67
Omar Li          Khaled Nasser   WHO           7.33

```

[cite: 1]

#### Query: Delegate Attendance Aggregation

Aggregates attendance status counts per delegate using conditional aggregation[cite: 1, 2]:

```sql
SELECT
    d.f_name || ' ' || d.l_name AS delegate_name,
    SUM(CASE WHEN a.status IN ('present', 'present_and_voting') THEN 1 ELSE 0 END) AS sessions_present,
    SUM(CASE WHEN a.status = 'late' THEN 1 ELSE 0 END) AS sessions_late,
    SUM(CASE WHEN a.status = 'absent' THEN 1 ELSE 0 END) AS sessions_absent
FROM delegate d
LEFT JOIN attendance a ON d.d_id = a.d_id
GROUP BY d.f_name, d.l_name
ORDER BY delegate_name;

```

**Expected Output:**

```text
DELEGATE_NAME    SESSIONS_PRESENT SESSIONS_LATE SESSIONS_ABSENT
---------------- ---------------- ------------- ---------------
Ahmed Mohsen                    2             1               0
Daniel Zakhari                  2             0               1
Fedaa Elmahdi                   3             0               0
Omar Li                         1             1               1
Renad Hamad                     2             1               0
Sara Khan                       3             0               0

```

[cite: 1]

#### Query: Overall Delegate Performance Ranking

Calculates composite rankings combining position paper scores (normalized to 10) and average chair evaluation marks[cite: 1, 2]:

```sql
SELECT
    d.d_id,
    d.f_name || ' ' || d.l_name AS delegate_name,
    cm.com_name,
    pp.score AS paper_score,
    ROUND(AVG(es.score_ev), 2) AS avg_evaluation_score,
    ROUND(((pp.score / 10) + AVG(es.score_ev)) / 2, 2) AS overall_score
FROM delegate d
JOIN committee cm ON d.com_id = cm.com_id
JOIN position_paper pp ON d.d_id = pp.d_id
LEFT JOIN evaluation_scores es ON d.d_id = es.d_id AND d.com_id = es.com_id
GROUP BY d.d_id, d.f_name, d.l_name, cm.com_name, pp.score
ORDER BY overall_score DESC;

```

**Expected Output:**

```text
D_ID DELEGATE_NAME  COM_NAME PAPER_SCORE AVG_EVALUATION_SCORE OVERALL_SCORE
---- -------------- -------- ----------- -------------------- -------------
 105 Sara Khan      WHO               94                 9.33          9.37
 101 Ahmed Mohsen   ECOSOC            92                 8.67          8.94
 104 Renad Hamad    UNSC              90                 8.67          8.84
 102 Fedaa Elmahdi  ECOSOC            88                 7.67          8.24
 103 Daniel Zakhari UNSC              85                 7.67          8.09
 106 Omar Li        WHO               87                 7.33          8.02

```

[cite: 1]

---

## Project Structure

```text
ausmun-management-system/
│
├── sql/
│   └── AUSMUN_CMP3201.sql               # Complete Oracle SQL script (DDL, constraints, seed data, queries)
│
├── docs/
│   └── AUSMUN Final Report.pdf          # Full engineering report (ER diagrams, relational maps, specs)
│
└── MUNDatabaseGUI/                      # Java Swing Maven Application Root
    ├── pom.xml                          # Maven build configuration and dependencies
    └── src/
        ├── main/
        │   ├── java/
        │   │   └── edu/
        │   │       └── aus/
        │   │           └── mundatabase/
        │   │               ├── MUNDatabaseGUI.java   # Application entry point (main method)
        │   │               ├── MainFrame.java        # Primary GUI controller, JDBC queries, and validators
        │   │               └── MainFrame.form        # NetBeans GUI Builder visual layout definition
        │   └── resources/                            # UI icons, themes, and configuration properties
        └── test/
            └── java/                                 # Unit tests for domain logic and input validators

```

[cite: 1, 2, 3]
