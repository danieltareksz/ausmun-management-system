

CREATE TABLE country (
    country_id     NUMBER(4),
    region         VARCHAR2(50) CONSTRAINT country_region_nn NOT NULL,
    country_name   VARCHAR2(80) CONSTRAINT country_name_nn NOT NULL,
    veto_power     CHAR(1) DEFAULT 'N' CONSTRAINT country_veto_nn NOT NULL,

    CONSTRAINT country_pk PRIMARY KEY (country_id),
    CONSTRAINT country_name_uk UNIQUE (country_name),
    CONSTRAINT country_veto_ck CHECK (veto_power IN ('Y', 'N'))
);

CREATE TABLE committee (
    com_id         NUMBER(4),
    com_name       VARCHAR2(100) CONSTRAINT committee_name_nn NOT NULL,
    com_type       VARCHAR2(50) CONSTRAINT committee_type_nn NOT NULL,
    building_name  VARCHAR2(80),
    room_no        VARCHAR2(20),

    CONSTRAINT committee_pk PRIMARY KEY (com_id),
    CONSTRAINT committee_name_uk UNIQUE (com_name)
);

CREATE TABLE workshop (
    workshop_id   NUMBER(4),
    title         VARCHAR2(120) CONSTRAINT workshop_title_nn NOT NULL,
    speaker_name  VARCHAR2(100),
    room_no       VARCHAR2(20),

    CONSTRAINT workshop_pk PRIMARY KEY (workshop_id)
);

CREATE TABLE mun_session (
    session_id     NUMBER(4),
    session_date   DATE CONSTRAINT session_date_nn NOT NULL,
    s_starttime    VARCHAR2(5) CONSTRAINT session_start_nn NOT NULL,
    s_endtime      VARCHAR2(5) CONSTRAINT session_end_nn NOT NULL,

    CONSTRAINT mun_session_pk PRIMARY KEY (session_id)
);

CREATE TABLE delegate (
    d_id          NUMBER(4),
    f_name        VARCHAR2(50) CONSTRAINT delegate_fname_nn NOT NULL,
    l_name        VARCHAR2(50) CONSTRAINT delegate_lname_nn NOT NULL,
    email         VARCHAR2(120) CONSTRAINT delegate_email_nn NOT NULL,
    school        VARCHAR2(120),
    dob           DATE CONSTRAINT delegate_dob_nn NOT NULL,
    regi_status   VARCHAR2(20) DEFAULT 'Registered' CONSTRAINT delegate_status_nn NOT NULL,
    country_id    NUMBER(4) CONSTRAINT delegate_country_nn NOT NULL,
    com_id        NUMBER(4) CONSTRAINT delegate_committee_nn NOT NULL,

    CONSTRAINT delegate_pk PRIMARY KEY (d_id),
    CONSTRAINT delegate_email_uk UNIQUE (email),
    CONSTRAINT delegate_did_com_uk UNIQUE (d_id, com_id),
    CONSTRAINT delegate_status_ck CHECK (regi_status IN ('Registered', 'Pending', 'Waitlisted', 'Cancelled')),
    CONSTRAINT delegate_country_fk FOREIGN KEY (country_id) REFERENCES country(country_id),
    CONSTRAINT delegate_committee_fk FOREIGN KEY (com_id) REFERENCES committee(com_id)
);

CREATE TABLE chair (
    chair_id       NUMBER(4),
    f_name         VARCHAR2(50) CONSTRAINT chair_fname_nn NOT NULL,
    l_name         VARCHAR2(50) CONSTRAINT chair_lname_nn NOT NULL,
    chair_email    VARCHAR2(120) CONSTRAINT chair_email_nn NOT NULL,
    assigned_topic VARCHAR2(150),
    com_id         NUMBER(4) CONSTRAINT chair_committee_nn NOT NULL,

    CONSTRAINT chair_pk PRIMARY KEY (chair_id),
    CONSTRAINT chair_email_uk UNIQUE (chair_email),
    CONSTRAINT chair_id_com_uk UNIQUE (chair_id, com_id),
    CONSTRAINT chair_committee_fk FOREIGN KEY (com_id) REFERENCES committee(com_id)
);

CREATE TABLE awards (
    aw_id       NUMBER(4),
    aw_title    VARCHAR2(100) CONSTRAINT awards_title_nn NOT NULL,
    a_date      DATE,
    com_id      NUMBER(4) CONSTRAINT awards_committee_nn NOT NULL,

    CONSTRAINT awards_pk PRIMARY KEY (aw_id),
    CONSTRAINT awards_committee_fk FOREIGN KEY (com_id) REFERENCES committee(com_id)
);

CREATE TABLE position_paper (
    paper_no    NUMBER(4),
    sub_date    DATE,
    content     VARCHAR2(2000),
    score       NUMBER(5,2),
    d_id        NUMBER(4) CONSTRAINT pp_delegate_nn NOT NULL,
    com_id      NUMBER(4) CONSTRAINT pp_committee_nn NOT NULL,

    CONSTRAINT position_paper_pk PRIMARY KEY (paper_no),
    CONSTRAINT pp_score_ck CHECK (score BETWEEN 0 AND 100),
    CONSTRAINT pp_delegate_uk UNIQUE (d_id),
    CONSTRAINT pp_delegate_fk FOREIGN KEY (d_id) REFERENCES delegate(d_id),
    CONSTRAINT pp_committee_fk FOREIGN KEY (com_id) REFERENCES committee(com_id),
    CONSTRAINT pp_delegate_com_fk FOREIGN KEY (d_id, com_id) REFERENCES delegate(d_id, com_id)
);


CREATE TABLE attendance (
    d_id        NUMBER(4),
    session_id  NUMBER(4),
    status      VARCHAR2(20) CONSTRAINT attendance_status_nn NOT NULL,

    CONSTRAINT attendance_pk PRIMARY KEY (d_id, session_id),
    CONSTRAINT attendance_status_ck CHECK (status IN ('present', 'present_and_voting', 'absent', 'late')),
    CONSTRAINT attendance_delegate_fk FOREIGN KEY (d_id) REFERENCES delegate(d_id),
    CONSTRAINT attendance_session_fk FOREIGN KEY (session_id) REFERENCES mun_session(session_id)
);

CREATE TABLE workshop_attendees (
    d_id         NUMBER(4),
    workshop_id  NUMBER(4),

    CONSTRAINT workshop_attendees_pk PRIMARY KEY (d_id, workshop_id),
    CONSTRAINT wa_delegate_fk FOREIGN KEY (d_id) REFERENCES delegate(d_id),
    CONSTRAINT wa_workshop_fk FOREIGN KEY (workshop_id) REFERENCES workshop(workshop_id)
);

CREATE TABLE award_recipients (
    d_id   NUMBER(4),
    aw_id  NUMBER(4),

    CONSTRAINT award_recipients_pk PRIMARY KEY (d_id, aw_id),
    CONSTRAINT ar_delegate_fk FOREIGN KEY (d_id) REFERENCES delegate(d_id),
    CONSTRAINT ar_award_fk FOREIGN KEY (aw_id) REFERENCES awards(aw_id)
);

CREATE TABLE topic (
    topic   VARCHAR2(150),
    com_id  NUMBER(4),

    CONSTRAINT topic_pk PRIMARY KEY (topic, com_id),
    CONSTRAINT topic_committee_fk FOREIGN KEY (com_id) REFERENCES committee(com_id)
);

CREATE TABLE evaluations (
    d_id      NUMBER(4),
    chair_id  NUMBER(4),
    com_id    NUMBER(4),

    CONSTRAINT evaluations_pk PRIMARY KEY (d_id, chair_id, com_id),
    CONSTRAINT eval_delegate_com_fk FOREIGN KEY (d_id, com_id) REFERENCES delegate(d_id, com_id),
    CONSTRAINT eval_chair_com_fk FOREIGN KEY (chair_id, com_id) REFERENCES chair(chair_id, com_id),
    CONSTRAINT eval_committee_fk FOREIGN KEY (com_id) REFERENCES committee(com_id)
);

CREATE TABLE feedback (
    d_id             NUMBER(4),
    chair_id         NUMBER(4),
    com_id           NUMBER(4),
    professionalism  NUMBER(2),
    research         NUMBER(2),
    p_speaking       NUMBER(2),

    CONSTRAINT feedback_pk PRIMARY KEY (d_id, chair_id, com_id, professionalism, research, p_speaking),
    CONSTRAINT feedback_eval_fk FOREIGN KEY (d_id, chair_id, com_id) REFERENCES evaluations(d_id, chair_id, com_id),
    CONSTRAINT feedback_prof_ck CHECK (professionalism BETWEEN 0 AND 10),
    CONSTRAINT feedback_research_ck CHECK (research BETWEEN 0 AND 10),
    CONSTRAINT feedback_speaking_ck CHECK (p_speaking BETWEEN 0 AND 10)
);

CREATE VIEW evaluation_scores AS
SELECT
    d_id,
    chair_id,
    com_id,
    ROUND(AVG((professionalism + research + p_speaking) / 3), 2) AS score_ev
FROM feedback
GROUP BY d_id, chair_id, com_id;


-- COUNTRY
INSERT INTO country VALUES (1, 'Middle East', 'United Arab Emirates', 'N');
INSERT INTO country VALUES (2, 'North America', 'United States of America', 'Y');
INSERT INTO country VALUES (3, 'Europe', 'France', 'Y');
INSERT INTO country VALUES (4, 'Africa', 'Egypt', 'N');
INSERT INTO country VALUES (5, 'Asia', 'India', 'N');
INSERT INTO country VALUES (6, 'Asia', 'China', 'Y');

-- COMMITTEE
INSERT INTO committee VALUES (10, 'ECOSOC', 'Specialized', 'ESB', '1001');
INSERT INTO committee VALUES (20, 'UNSC', 'Security Council', 'ESB', '1002');
INSERT INTO committee VALUES (30, 'WHO', 'Specialized', 'ESB', '1003');

-- TOPIC
INSERT INTO topic VALUES ('Youth unemployment in MENA', 10);
INSERT INTO topic VALUES ('Forced labor in global supply chains', 10);
INSERT INTO topic VALUES ('Cybersecurity and international peace', 20);
INSERT INTO topic VALUES ('Peacekeeping reform', 20);
INSERT INTO topic VALUES ('Global pandemic preparedness', 30);
INSERT INTO topic VALUES ('Mental health access', 30);

-- WORKSHOP
INSERT INTO workshop VALUES (501, 'Rules of Procedure Training', 'Dr. Mohamed Ibrahim', '2001');
INSERT INTO workshop VALUES (502, 'Public Speaking for Delegates', 'Youssef Hassan', '2002');
INSERT INTO workshop VALUES (503, 'Research and Position Paper Writing', 'Ranim Ali', '2003');

-- MUN_SESSION
INSERT INTO mun_session VALUES (1001, DATE '2026-01-30', '09:00', '10:30');
INSERT INTO mun_session VALUES (1002, DATE '2026-01-30', '11:00', '12:30');
INSERT INTO mun_session VALUES (1003, DATE '2026-01-31', '09:00', '10:30');

-- DELEGATE
INSERT INTO delegate VALUES (101, 'Ahmed', 'Mohsen', 'ahmed.mohsen@example.com', 'American University of Sharjah', DATE '2005-03-15', 'Registered', 4, 10);
INSERT INTO delegate VALUES (102, 'Fedaa', 'Elmahdi', 'fedaa.elmahdi@example.com', 'American University of Sharjah', DATE '2004-10-20', 'Registered', 1, 10);
INSERT INTO delegate VALUES (103, 'Daniel', 'Zakhari', 'daniel.zakhari@example.com', 'American University of Sharjah', DATE '2005-01-12', 'Registered', 2, 20);
INSERT INTO delegate VALUES (104, 'Renad', 'Hamad', 'renad.hamad@example.com', 'American University of Sharjah', DATE '2004-07-09', 'Registered', 3, 20);
INSERT INTO delegate VALUES (105, 'Sara', 'Khan', 'sara.khan@example.com', 'AUS High School', DATE '2006-05-11', 'Registered', 5, 30);
INSERT INTO delegate VALUES (106, 'Omar', 'Li', 'omar.li@example.com', 'Sharjah International School', DATE '2005-12-02', 'Registered', 6, 30);

-- CHAIR
INSERT INTO chair VALUES (201, 'Youssef', 'Hassan', 'youssef.hassan@example.com', 'Youth Employment', 10);
INSERT INTO chair VALUES (202, 'Ranim', 'Ali', 'ranim.ali@example.com', 'Labor Supply Chains', 10);
INSERT INTO chair VALUES (203, 'Shamma', 'Almualla', 'shamma.almualla@example.com', 'Cybersecurity', 20);
INSERT INTO chair VALUES (204, 'Reema', 'Shubair', 'reema.shubair@example.com', 'Peacekeeping', 20);
INSERT INTO chair VALUES (205, 'Mariam', 'Saeed', 'mariam.saeed@example.com', 'Pandemic Preparedness', 30);
INSERT INTO chair VALUES (206, 'Khaled', 'Nasser', 'khaled.nasser@example.com', 'Mental Health', 30);

-- AWARDS
INSERT INTO awards VALUES (301, 'Best Delegate', DATE '2026-02-01', 10);
INSERT INTO awards VALUES (302, 'Outstanding Delegate', DATE '2026-02-01', 10);
INSERT INTO awards VALUES (303, 'Honorable Mention', DATE '2026-02-01', 20);
INSERT INTO awards VALUES (304, 'Best Position Paper', DATE '2026-02-01', 30);

-- POSITION_PAPER
INSERT INTO position_paper VALUES (401, DATE '2026-01-25', 'Position paper on youth employment and labor policy.', 92, 101, 10);
INSERT INTO position_paper VALUES (402, DATE '2026-01-25', 'Position paper on labor rights and sustainable supply chains.', 88, 102, 10);
INSERT INTO position_paper VALUES (403, DATE '2026-01-26', 'Position paper on cybersecurity and peacekeeping.', 85, 103, 20);
INSERT INTO position_paper VALUES (404, DATE '2026-01-26', 'Position paper on UNSC reform and peacekeeping operations.', 90, 104, 20);
INSERT INTO position_paper VALUES (405, DATE '2026-01-27', 'Position paper on global pandemic preparedness.', 94, 105, 30);
INSERT INTO position_paper VALUES (406, DATE '2026-01-27', 'Position paper on mental health access and policy.', 87, 106, 30);

-- ATTENDANCE
INSERT INTO attendance VALUES (101, 1001, 'present_and_voting');
INSERT INTO attendance VALUES (101, 1002, 'present');
INSERT INTO attendance VALUES (101, 1003, 'late');

INSERT INTO attendance VALUES (102, 1001, 'present');
INSERT INTO attendance VALUES (102, 1002, 'present_and_voting');
INSERT INTO attendance VALUES (102, 1003, 'present');

INSERT INTO attendance VALUES (103, 1001, 'present');
INSERT INTO attendance VALUES (103, 1002, 'absent');
INSERT INTO attendance VALUES (103, 1003, 'present');

INSERT INTO attendance VALUES (104, 1001, 'late');
INSERT INTO attendance VALUES (104, 1002, 'present');
INSERT INTO attendance VALUES (104, 1003, 'present_and_voting');

INSERT INTO attendance VALUES (105, 1001, 'present');
INSERT INTO attendance VALUES (105, 1002, 'present');
INSERT INTO attendance VALUES (105, 1003, 'present_and_voting');

INSERT INTO attendance VALUES (106, 1001, 'absent');
INSERT INTO attendance VALUES (106, 1002, 'present');
INSERT INTO attendance VALUES (106, 1003, 'late');

-- WORKSHOP_ATTENDEES
INSERT INTO workshop_attendees VALUES (101, 501);
INSERT INTO workshop_attendees VALUES (101, 502);
INSERT INTO workshop_attendees VALUES (102, 501);
INSERT INTO workshop_attendees VALUES (103, 502);
INSERT INTO workshop_attendees VALUES (104, 503);
INSERT INTO workshop_attendees VALUES (105, 501);
INSERT INTO workshop_attendees VALUES (105, 503);
INSERT INTO workshop_attendees VALUES (106, 502);

-- AWARD_RECIPIENTS
INSERT INTO award_recipients VALUES (101, 301);
INSERT INTO award_recipients VALUES (102, 302);
INSERT INTO award_recipients VALUES (101, 302);
INSERT INTO award_recipients VALUES (103, 303);
INSERT INTO award_recipients VALUES (104, 303);
INSERT INTO award_recipients VALUES (105, 304);

-- EVALUATIONS
INSERT INTO evaluations VALUES (101, 201, 10);
INSERT INTO evaluations VALUES (101, 202, 10);
INSERT INTO evaluations VALUES (102, 201, 10);
INSERT INTO evaluations VALUES (103, 203, 20);
INSERT INTO evaluations VALUES (104, 204, 20);
INSERT INTO evaluations VALUES (105, 205, 30);
INSERT INTO evaluations VALUES (106, 206, 30);

-- FEEDBACK
INSERT INTO feedback VALUES (101, 201, 10, 9, 9, 8);
INSERT INTO feedback VALUES (101, 202, 10, 8, 9, 9);
INSERT INTO feedback VALUES (102, 201, 10, 8, 8, 7);
INSERT INTO feedback VALUES (103, 203, 20, 7, 8, 8);
INSERT INTO feedback VALUES (104, 204, 20, 9, 8, 9);
INSERT INTO feedback VALUES (105, 205, 30, 10, 9, 9);
INSERT INTO feedback VALUES (106, 206, 30, 7, 7, 8);

COMMIT;


-- Query 1: Show all delegates with their country and committee.
SELECT
    d.d_id,
    d.f_name || ' ' || d.l_name AS delegate_name,
    c.country_name,
    cm.com_name,
    d.regi_status
FROM delegate d
JOIN country c ON d.country_id = c.country_id
JOIN committee cm ON d.com_id = cm.com_id
ORDER BY d.d_id;

-- Query 2: Count delegates in each committee.
SELECT
    cm.com_name,
    COUNT(d.d_id) AS number_of_delegates
FROM committee cm
LEFT JOIN delegate d ON cm.com_id = d.com_id
GROUP BY cm.com_name
ORDER BY cm.com_name;

-- Query 3: Show committee topics.
SELECT
    cm.com_name,
    t.topic
FROM committee cm
JOIN topic t ON cm.com_id = t.com_id
ORDER BY cm.com_name, t.topic;

-- Query 4: Show attendance records with delegate names and session dates.
SELECT
    d.f_name || ' ' || d.l_name AS delegate_name,
    s.session_date,
    s.s_starttime,
    s.s_endtime,
    a.status
FROM attendance a
JOIN delegate d ON a.d_id = d.d_id
JOIN mun_session s ON a.session_id = s.session_id
ORDER BY d.d_id, s.session_id;

-- Query 5: Attendance summary per delegate.
SELECT
    d.f_name || ' ' || d.l_name AS delegate_name,
    SUM(CASE WHEN a.status IN ('present', 'present_and_voting') THEN 1 ELSE 0 END) AS sessions_present,
    SUM(CASE WHEN a.status = 'late' THEN 1 ELSE 0 END) AS sessions_late,
    SUM(CASE WHEN a.status = 'absent' THEN 1 ELSE 0 END) AS sessions_absent
FROM delegate d
LEFT JOIN attendance a ON d.d_id = a.d_id
GROUP BY d.f_name, d.l_name
ORDER BY delegate_name;

-- Query 6: Show position papers with scores.
SELECT
    pp.paper_no,
    d.f_name || ' ' || d.l_name AS delegate_name,
    cm.com_name,
    pp.sub_date,
    pp.score
FROM position_paper pp
JOIN delegate d ON pp.d_id = d.d_id
JOIN committee cm ON pp.com_id = cm.com_id
ORDER BY pp.score DESC;

-- Query 7: Show derived evaluation scores.
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

-- Query 8: Show award recipients.
SELECT
    aw.aw_title,
    cm.com_name,
    d.f_name || ' ' || d.l_name AS recipient_name,
    aw.a_date
FROM award_recipients ar
JOIN awards aw ON ar.aw_id = aw.aw_id
JOIN delegate d ON ar.d_id = d.d_id
JOIN committee cm ON aw.com_id = cm.com_id
ORDER BY aw.aw_title, recipient_name;

-- Query 9: Show workshop attendees.
SELECT
    w.title AS workshop_title,
    w.speaker_name,
    d.f_name || ' ' || d.l_name AS delegate_name
FROM workshop_attendees wa
JOIN workshop w ON wa.workshop_id = w.workshop_id
JOIN delegate d ON wa.d_id = d.d_id
ORDER BY w.title, delegate_name;

-- Query 10: Verify each committee has at least two chairs.
SELECT
    cm.com_name,
    COUNT(ch.chair_id) AS number_of_chairs
FROM committee cm
LEFT JOIN chair ch ON cm.com_id = ch.com_id
GROUP BY cm.com_name
HAVING COUNT(ch.chair_id) >= 2
ORDER BY cm.com_name;

-- Query 11: Overall delegate performance using paper score and evaluation score.
SELECT
    d.d_id,
    d.f_name || ' ' || d.l_name AS delegate_name,
    cm.com_name,
    pp.score AS paper_score,
    ROUND(AVG(es.score_ev), 2) AS avg_evaluation_score,
    ROUND((pp.score / 10 + AVG(es.score_ev)) / 2, 2) AS overall_score
FROM delegate d
JOIN committee cm ON d.com_id = cm.com_id
JOIN position_paper pp ON d.d_id = pp.d_id
LEFT JOIN evaluation_scores es ON d.d_id = es.d_id AND d.com_id = es.com_id
GROUP BY d.d_id, d.f_name, d.l_name, cm.com_name, pp.score
ORDER BY overall_score DESC;

-- Query 12: Show full feedback components for each evaluation.
SELECT
    d.f_name || ' ' || d.l_name AS delegate_name,
    ch.f_name || ' ' || ch.l_name AS chair_name,
    cm.com_name,
    f.professionalism,
    f.research,
    f.p_speaking,
    ROUND((f.professionalism + f.research + f.p_speaking) / 3, 2) AS derived_score
FROM feedback f
JOIN delegate d ON f.d_id = d.d_id
JOIN chair ch ON f.chair_id = ch.chair_id
JOIN committee cm ON f.com_id = cm.com_id
ORDER BY derived_score DESC;

