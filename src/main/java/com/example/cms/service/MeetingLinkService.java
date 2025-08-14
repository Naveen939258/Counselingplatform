package com.example.cms.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.example.cms.entity.MeetingLink;
import com.example.cms.repository.MeetingLinkRepository;

@Service
public class MeetingLinkService {

    @Autowired
    private MeetingLinkRepository mlr;

    public void storeMeetings(MeetingLink ml) {
        mlr.save(ml);
    }

    // ✅ Check if a meeting exists for a given student
    public boolean meetingForStudentExists(long studentId) {
        return mlr.existsByStudentId(studentId);
    }

    public List<MeetingLink> getAllRecords() {
        return mlr.findAll();
    }

    public List<MeetingLink> getAllMeetings() {
        return mlr.findAll();
    }
}
