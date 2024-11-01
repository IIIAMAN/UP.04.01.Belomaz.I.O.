package org.example.up_itog.controllers;

import jakarta.servlet.http.HttpSession;
import org.example.up_itog.models.*;
import org.example.up_itog.repositories.ClientRepository;
import org.example.up_itog.repositories.UserRepository;
import org.example.up_itog.repositories.GradeRepository;
import org.example.up_itog.repositories.ScheduleRepository;
import org.example.up_itog.repositories.ClientCourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/student")
public class StudentController {

    @Autowired
    private ClientCourseRepository clientCourseRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @GetMapping("/home")
    public String getHomePage(Model model, @AuthenticationPrincipal UserTable user) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = ((User) auth.getPrincipal()).getUsername();
        UserTable currentUser = userRepository.findByUsername(username);

        model.addAttribute("fullName", clientRepository.findByUserTable(currentUser).getFullName());

        return "student/studentHome";
    }

    @GetMapping("/profile")
    public String getProfile(Model model, Principal principal) {
        String username = principal.getName();
        UserTable user = userRepository.findByUsername(username);
        Client client = clientRepository.findByUserTable(user);

        model.addAttribute("client", client);
        model.addAttribute("username", username);

        return "student/profile";
    }


    @GetMapping("/profile/edit")
    public String editProfileForm(Model model, Principal principal) {
        String username = principal.getName();
        UserTable user = userRepository.findByUsername(username);
        Client client = clientRepository.findByUserTable(user);

        model.addAttribute("client", client);
        model.addAttribute("username", username);

        return "student/profile_edit";
    }

    @PostMapping("/profile/edit")
    public ResponseEntity<String> editProfile(
            @RequestParam("id") Long id,
            @RequestParam("firstName") String firstName,
            @RequestParam("lastName") String lastName,
            @RequestParam("middleName") String middleName,
            @RequestParam("username") String username,
            @RequestParam("password") String password,
            @RequestParam("age") Integer age,
            HttpSession session) {

        Client client = clientRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Клиент не найден"));

        UserTable existingUser = userRepository.findByUsername(username);
        if (existingUser != null && !existingUser.getId().equals(client.getUserTable().getId())) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Этот логин уже занят. Пожалуйста, выберите другой.");
        }

        client.setFullName(firstName + " " + lastName + " " + middleName);
        client.setAge(age);

        UserTable user = client.getUserTable();
        boolean isLoginChanged = !user.getUsername().equals(username);

        user.setUsername(username);
        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        userRepository.save(user);
        clientRepository.save(client);

        if (isLoginChanged || password != null && !password.isEmpty()) {
            session.invalidate();
        }

        return ResponseEntity.ok("Успешно обновлено");
    }





    @GetMapping("/grades")
    public String getGrades(Model model, Principal principal) {
        String username = principal.getName();
        UserTable user = userRepository.findByUsername(username);
        Client client = clientRepository.findByUserTable(user);

        if (client != null) {
            List<Grade> grades = gradeRepository.findByStudent(client);
            model.addAttribute("grades", grades);
        } else {
            model.addAttribute("error", "Клиент не найден.");
        }

        return "student/grades";
    }

    @GetMapping("/schedule")
    public String getSchedule(Model model, Principal principal) {
        String username = principal.getName();
        UserTable user = userRepository.findByUsername(username);
        Client client = clientRepository.findByUserTable(user);

        if (client != null) {
            List<Course> courses = clientCourseRepository.findCoursesByClientId(client.getId());
            List<Schedule> schedule = scheduleRepository.findByCourseIn(courses);
            model.addAttribute("schedule", schedule);
        } else {
            model.addAttribute("error", "Клиент не найден.");
        }

        return "student/schedule";
    }
}
