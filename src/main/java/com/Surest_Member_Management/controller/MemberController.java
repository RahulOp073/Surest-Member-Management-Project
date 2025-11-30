package com.Surest_Member_Management.controller;

import com.Surest_Member_Management.entity.Member;
import com.Surest_Member_Management.repository.MemberRepository;
import org.springframework.data.domain.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.CacheEvict;

@RestController
@RequestMapping("/members")
public class MemberController {

    private final MemberRepository memberRepository;

    public MemberController(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @GetMapping("/show")
    public ResponseEntity<?> getMembers(
            @RequestParam(required = false) String firstName,
            @RequestParam(required = false) String lastName,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "lastName,asc") String sort
    ) {
        String[] sortParams = sort.split(",");
        String sortField = sortParams[0];
        String sortDirection = sortParams.length > 1 ? sortParams[1] : "asc";

        Sort sorting = sortDirection.equalsIgnoreCase("asc")
                ? Sort.by(sortField).ascending()
                : Sort.by(sortField).descending();

        Pageable pageable = PageRequest.of(page, size, sorting);

        Page<Member> result = memberRepository.searchMembers(firstName, lastName, pageable);
        return ResponseEntity.ok(result);
    }

    @Cacheable(value = "members", key = "#id")
    @GetMapping("/{id}")
    public ResponseEntity<?> getMemberById(
            @PathVariable UUID id,
            @RequestHeader("Authorization") String authHeader) {

        String role = extractRoleFromJwt(authHeader);
        if (role == null) {
            return ResponseEntity.status(401).body("Invalid or missing token");
        }

        Optional<Member> optionalMember = memberRepository.findById(id);

        if (optionalMember.isPresent()) {
            return ResponseEntity.ok(optionalMember.get());
        } else {
            return ResponseEntity.status(404).body("Member not found");
        }
    }

    @PostMapping("/add")
    public ResponseEntity<?> createMember(@RequestBody Member member) {
        if (member.getFirstName() == null || member.getLastName() == null) {
            return ResponseEntity.badRequest().body("firstName and lastName are required");
        }
        Member saved = memberRepository.save(member);
        return ResponseEntity.status(201).body(saved);
    }
    @CacheEvict(value = "members", key = "#id")
    @PutMapping("/{id}")
    public ResponseEntity<?> updateMember(@PathVariable UUID id, @RequestBody Member updated) {
        Optional<Member> optionalMember = memberRepository.findById(id);
        if (optionalMember.isPresent()) {
            Member existing = optionalMember.get();
            existing.setFirstName(updated.getFirstName());
            existing.setLastName(updated.getLastName());
            existing.setEmail(updated.getEmail());
            memberRepository.save(existing);
            return ResponseEntity.ok(existing);
        } else {
            return ResponseEntity.status(404).body("Member not found");
        }
    }

    @CacheEvict(value = "members", key = "#id")
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMember(@PathVariable UUID id) {
        if (!memberRepository.existsById(id)) {
            return ResponseEntity.status(404).body("Member not found");
        }
        memberRepository.deleteById(id);
        return ResponseEntity.status(204).build();
    }
    String extractRoleFromJwt(String authHeader) {
        return "ADMIN";
    }
}
