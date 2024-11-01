package org.example.up_itog.controllers;

import org.example.up_itog.models.UserTable;
import org.example.up_itog.models.Role;
import org.example.up_itog.models.Client;
import org.example.up_itog.models.Groupss;
import org.example.up_itog.repositories.ClientRepository;
import org.example.up_itog.repositories.UserRepository;
import org.example.up_itog.repositories.RoleRepository;
import org.example.up_itog.repositories.GroupRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Collections;
import java.util.List;

@Controller
public class AuthController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/auth/signup")
    public String showRegisterForm(Model model) {
        List<Groupss> groups = groupRepository.findAll();
        model.addAttribute("groups", groups);
        return "register";
    }

    @PostMapping("/auth/register")
    public String registerUser(
            @RequestParam String username,
            @RequestParam String password,
            @RequestParam String role,
            @RequestParam String firstName,
            @RequestParam String lastName,
            @RequestParam String patronymic,
            @RequestParam int age,
            Model model) {

        if (username.trim().length() < 5 || password.trim().length() < 5) {
            model.addAttribute("message", "Логин и пароль должны содержать минимум 5 символов и не могут содержать пробелы.");
            return "register";
        }

        if (username.contains(" ") || password.contains(" ")) {
            model.addAttribute("message", "Логин и пароль не могут содержать пробелы.");
            return "register";
        }


        if (!firstName.matches("[A-Za-zА-Яа-яЁё]+") || firstName.trim().length() < 2 ||
                !lastName.matches("[A-Za-zА-Яа-яЁё]+") || lastName.trim().length() < 2 ||
                !patronymic.matches("[A-Za-zА-Яа-яЁё]+") || patronymic.trim().length() < 2) {
            model.addAttribute("message", "Имя, фамилия и отчество должны содержать только буквы и минимум 2 символа.");
            return "register";
        }

        UserTable existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            model.addAttribute("message", "Пользователь с таким логином уже существует");
            return "register";
        }

        UserTable user = new UserTable();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));

        Role userRole = roleRepository.findByName(role.toUpperCase());
        if (userRole == null) {
            userRole = new Role(role.toUpperCase());
            roleRepository.save(userRole);
        }

        user.setRoles(Collections.singleton(userRole));
        userRepository.save(user);

        Groupss defaultGroup = groupRepository.findByName("НЕ РАСПРЕДЕЛЕН");
        if (defaultGroup == null) {
            defaultGroup = new Groupss();
            defaultGroup.setName("НЕ РАСПРЕДЕЛЕН");
            groupRepository.save(defaultGroup);
        }

        Client client = new Client();
        client.setUserTable(user);
        client.setGroup(defaultGroup);


        String fullName = firstName + " " + lastName + " " + patronymic;
        client.setFullName(fullName);
        client.setAge(age);

        clientRepository.save(client);

        return "redirect:/auth/login?registerSuccess";
    }
}



