package com.Surest_Member_Management.controller;


import com.Surest_Member_Management.entity.Role;
import com.Surest_Member_Management.repository.RoleRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/roles")
public class RoleController {

    @Autowired
    private RoleRepository roleRepository;

    @PostMapping("/add")
    public Role addRole(@RequestBody Role role) {
        role.setId(UUID.randomUUID());
        return roleRepository.save(role);
    }

    @GetMapping("/all")
    public List<Role> getAllRoles() {
        return roleRepository.findAll();
    }
}
