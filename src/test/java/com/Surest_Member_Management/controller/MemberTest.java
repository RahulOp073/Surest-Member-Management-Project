package com.Surest_Member_Management.controller;

import com.Surest_Member_Management.entity.Member;
import com.Surest_Member_Management.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MemberControllerTest {

    @Mock
    private MemberRepository memberRepository;

    @Spy
    @InjectMocks
    private MemberController memberController;
    @Test
    void testGetMembers() {
        Member m1 = new Member(UUID.randomUUID(), "John", "Doe", "john@example.com");
        Page<Member> page = new PageImpl<>(List.of(m1));

        when(memberRepository.searchMembers(anyString(), anyString(), any(Pageable.class)))
                .thenReturn(page);

        ResponseEntity<?> response = memberController.getMembers(
                "John", "Doe", 0, 10, "lastName,asc"
        );

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(((Page<?>) response.getBody()).getContent().contains(m1));
    }
    @Test
    void testGetMemberById_validToken() {
        UUID id = UUID.randomUUID();
        Member m = new Member(id, "John", "Doe", "john@example.com");

        doReturn("ADMIN").when(memberController).extractRoleFromJwt(anyString());
        when(memberRepository.findById(id)).thenReturn(Optional.of(m));

        ResponseEntity<?> response = memberController.getMemberById(id, "Bearer token");

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(m, response.getBody());
    }
    @Test
    void testGetMemberById_invalidToken() {
        UUID id = UUID.randomUUID();

        doReturn(null).when(memberController).extractRoleFromJwt(anyString());

        ResponseEntity<?> response = memberController.getMemberById(id, "Bearer invalid");

        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid or missing token", response.getBody());
    }
    @Test
    void testGetMemberById_notFound() {
        UUID id = UUID.randomUUID();

        doReturn("ADMIN").when(memberController).extractRoleFromJwt(anyString());
        when(memberRepository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<?> response = memberController.getMemberById(id, "Bearer token");

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("Member not found", response.getBody());
    }
    @Test
    void testCreateMember_success() {
        Member m = new Member(UUID.randomUUID(), "John", "Doe", "john@example.com");

        when(memberRepository.save(any())).thenReturn(m);

        ResponseEntity<?> resp = memberController.createMember(m);

        assertEquals(HttpStatus.CREATED, resp.getStatusCode());
        assertEquals(m, resp.getBody());
    }
    @Test
    void testCreateMember_missingFields() {
        Member m = new Member();
        m.setFirstName(null);

        ResponseEntity<?> resp = memberController.createMember(m);

        assertEquals(HttpStatus.BAD_REQUEST, resp.getStatusCode());
        assertEquals("firstName and lastName are required", resp.getBody());
    }
    @Test
    void testUpdateMember_success() {
        UUID id = UUID.randomUUID();
        Member existing = new Member(id, "John", "Doe", "john@example.com");
        Member updated = new Member(id, "Jane", "Smith", "jane@example.com");

        when(memberRepository.findById(id)).thenReturn(Optional.of(existing));
        when(memberRepository.save(any())).thenReturn(existing);

        ResponseEntity<?> resp = memberController.updateMember(id, updated);

        assertEquals(HttpStatus.OK, resp.getStatusCode());
        Member returned = (Member) resp.getBody();
        assertEquals("Jane", returned.getFirstName());
        assertEquals("Smith", returned.getLastName());
    }
    @Test
    void testUpdateMember_notFound() {
        UUID id = UUID.randomUUID();
        Member updated = new Member(id, "Jane", "Smith", "jane@example.com");

        when(memberRepository.findById(id)).thenReturn(Optional.empty());

        ResponseEntity<?> resp = memberController.updateMember(id, updated);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertEquals("Member not found", resp.getBody());
    }
    @Test
    void testDeleteMember_success() {
        UUID id = UUID.randomUUID();

        when(memberRepository.existsById(id)).thenReturn(true);
        doNothing().when(memberRepository).deleteById(id);

        ResponseEntity<?> resp = memberController.deleteMember(id);

        assertEquals(HttpStatus.NO_CONTENT, resp.getStatusCode());
        verify(memberRepository, times(1)).deleteById(id);
    }
    @Test
    void testDeleteMember_notFound() {
        UUID id = UUID.randomUUID();

        when(memberRepository.existsById(id)).thenReturn(false);

        ResponseEntity<?> resp = memberController.deleteMember(id);

        assertEquals(HttpStatus.NOT_FOUND, resp.getStatusCode());
        assertEquals("Member not found", resp.getBody());
    }
}
