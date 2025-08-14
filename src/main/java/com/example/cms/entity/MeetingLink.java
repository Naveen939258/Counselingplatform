package com.example.cms.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Table;

@Entity
@Table(name = "Meeting")
public class MeetingLink {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "date")
    private String appt;

    @Column(name = "time")
    private String time;

    @Column(name = "Meeting_Link")
    private String meetingLink;

    @Column(name = "student_id") // optional if you store student reference
    private Long studentId;

    @Column(name = "counselor_id") // optional if you store counselor reference
    private int counselorId;

    public MeetingLink() { }

    public MeetingLink(String appt, String time, String meetingLink, Long studentId, int counselorId) {
        this.appt = appt;
        this.time = time;
        this.meetingLink = meetingLink;
        this.studentId = studentId;
        this.counselorId = counselorId;
    }
    // getters and setters
    public Long getId() {
        return id;
    }
    public void setId(Long id) {
        this.id = id;
    }

    public String getAppt() {
        return appt;
    }
    public void setAppt(String appt) {
        this.appt = appt;
    }

    public String getTime() {
        return time;
    }
    public void setTime(String time) {
        this.time = time;
    }

    public String getMeetingLink() {
        return meetingLink;
    }
    public void setMeetingLink(String meetingLink) {
        this.meetingLink = meetingLink;
    }

    public Long getStudentId() {
        return studentId;
    }
    public void setStudentId(Long studentId) {
        this.studentId = studentId;
    }

    public int getCounselorId() {
        return counselorId;
    }
    public void setCounselorId(int counselorId) {
        this.counselorId = counselorId;
    }
}
