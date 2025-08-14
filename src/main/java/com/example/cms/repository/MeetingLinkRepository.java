package com.example.cms.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.cms.entity.MeetingLink;

public interface MeetingLinkRepository extends JpaRepository<MeetingLink, Long> {
	boolean existsByStudentId(Long studentId);
}

