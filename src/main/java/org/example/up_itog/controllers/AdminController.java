package org.example.up_itog.controllers;

import org.example.up_itog.models.*;
import org.example.up_itog.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.*;

@Controller
public class AdminController {

    @Autowired
    private ClientCourseRepository clientCourseRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private GroupRepository groupRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private SubjectRepository subjectRepository;

    @GetMapping("/admin/home")
    public String adminHome() {
        return "admin/adminHome";
    }


    @GetMapping("/admin/manage")
    public String manageUsers(Model model) {
        List<UserTable> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/manageUsers";
    }

    @GetMapping("/admin/add-user")
    public String showAddUserForm(Model model) {
        List<Role> roles = roleRepository.findAll();
        model.addAttribute("roles", roles);
        return "admin/addUser";
    }

    @PostMapping("/admin/save-user")
    public String saveUser(@RequestParam String username, @RequestParam String password, @RequestParam String role, Model model) {
        UserTable existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            model.addAttribute("message", "Пользователь с таким логином уже существует");
            return "admin/addUser";
        }

        UserTable user = new UserTable();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        Role userRole = roleRepository.findByName(role.toUpperCase());
        if (userRole == null) {
            userRole = new Role(role.toUpperCase());
            roleRepository.save(userRole);
        }

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);
        userRepository.save(user);
        return "redirect:/admin/manage";
    }

    @GetMapping("/admin/edit-user")
    public String showEditUserForm(@RequestParam Long id, Model model) {
        UserTable user = userRepository.findById(id).orElse(null);
        if (user != null) {
            model.addAttribute("user", user);
            model.addAttribute("roles", roleRepository.findAll());
        }
        return "admin/editUser";
    }

    @PostMapping("/admin/update-user")
    public String updateUser(@RequestParam Long id, @RequestParam String username,
                             @RequestParam(required = false) String password, @RequestParam String role) {
        UserTable user = userRepository.findById(id).orElse(null);
        if (user != null) {
            user.setUsername(username);
            if (password != null && !password.isEmpty()) {
                user.setPassword(passwordEncoder.encode(password));
            }

            Role userRole = roleRepository.findByName(role.toUpperCase());
            if (userRole == null) {
                userRole = new Role(role.toUpperCase());
                roleRepository.save(userRole);
            }

            Set<Role> roles = new HashSet<>();
            roles.add(userRole);
            user.setRoles(roles);

            userRepository.save(user);
        }
        return "redirect:/admin/manage";
    }

    @GetMapping("/admin/delete-user")
    public String deleteUser(@RequestParam Long id) {
        userRepository.deleteById(id);

        return "redirect:/admin/manage";
    }











    @GetMapping("/admin/manage-subjects")
    public String manageSubjects(Model model) {
        List<Subject> subjects = subjectRepository.findAll();
        model.addAttribute("subjects", subjects);
        return "admin/manageSubjects";
    }

    @GetMapping("/admin/add-subject")
    public String showAddSubjectForm(Model model) {
        model.addAttribute("subject", new Subject());
        return "admin/addSubject";
    }

    @PostMapping("/admin/save-subject")
    public String saveSubject(@ModelAttribute Subject subject, Model model) {
        String trimmedName = subject.getName().replaceAll("\\s+", "");

        if (!trimmedName.matches("^[A-Za-zА-Яа-яЁё]+$") || trimmedName.length() < 3) {
            model.addAttribute("error", "Имя предмета должно содержать только буквы и минимум 3 символа.");
            return "admin/addSubject";
        }

        if (subjectRepository.existsByNameIgnoreCase(subject.getName())) {
            model.addAttribute("error", "Предмет с таким названием уже существует.");
            return "admin/addSubject";
        }

        subjectRepository.save(subject);
        return "redirect:/admin/manage-subjects";
    }

    @GetMapping("/admin/edit-subject")
    public String showEditSubjectForm(@RequestParam Long id, Model model) {
        Subject subject = subjectRepository.findById(id).orElse(null);
        model.addAttribute("subject", subject);
        return "admin/editSubject";
    }

    @PostMapping("/admin/update-subject")
    public String updateSubject(@ModelAttribute Subject subject, Model model) {
        String trimmedName = subject.getName().replaceAll("\\s+", "");

        if (!trimmedName.matches("^[A-Za-zА-Яа-яЁё]+$") || trimmedName.length() < 3) {
            model.addAttribute("error", "Имя предмета должно содержать только буквы и минимум 3 символа.");
            model.addAttribute("subject", subject);
            return "admin/editSubject";
        }

        if (subjectRepository.existsByNameIgnoreCase(subject.getName())) {
            model.addAttribute("error", "Предмет с таким названием уже существует.");
            model.addAttribute("subject", subject);
            return "admin/editSubject";
        }

        subjectRepository.save(subject);
        return "redirect:/admin/manage-subjects";
    }


    @GetMapping("/admin/delete-subject")
    public String deleteSubject(@RequestParam Long id) {
        subjectRepository.deleteById(id);
        return "redirect:/admin/manage-subjects";
    }












    @GetMapping("/admin/schedule")
    public String manageSchedule(Model model) {
        List<Schedule> schedules = scheduleRepository.findAll();
        model.addAttribute("schedules", schedules);
        return "admin/manageSchedule";
    }

    @GetMapping("/admin/add-schedule")
    public String showAddScheduleForm(Model model) {
        model.addAttribute("courses", courseRepository.findAll());
        model.addAttribute("rooms", roomRepository.findAll());
        return "admin/addSchedule";
    }

    @PostMapping("/admin/save-schedule")
    public String saveSchedule(@RequestParam String dateTime, @RequestParam Long courseId, @RequestParam Long roomId, RedirectAttributes redirectAttributes) {

        LocalDateTime parsedDateTime;
        try {
            parsedDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("error", "Некорректная дата и время.");
            return "redirect:/admin/add-schedule";
        }

        int year = parsedDateTime.getYear();
        if (year < 2024 || year > 2025) {
            redirectAttributes.addFlashAttribute("error", "Год должен быть в пределах 2024-2025.");
            return "redirect:/admin/add-schedule";
        }


        if (scheduleRepository.existsByDateTimeAndCourseIdAndRoomId(parsedDateTime, courseId, roomId)) {
            redirectAttributes.addFlashAttribute("error", "Расписание с такими же курсом, кабинетом и временем уже существует.");
            return "redirect:/admin/add-schedule";
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + courseId));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid room Id:" + roomId));

        Schedule schedule = new Schedule();
        schedule.setDateTime(parsedDateTime);
        schedule.setCourse(course);
        schedule.setRoom(room);
        scheduleRepository.save(schedule);
        redirectAttributes.addFlashAttribute("success", "Расписание успешно добавлено!");
        return "redirect:/admin/schedule";
    }

    @GetMapping("/admin/edit-schedule")
    public String showEditScheduleForm(@RequestParam Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Schedule schedule = scheduleRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid schedule Id: " + id));

            model.addAttribute("schedule", schedule);
            model.addAttribute("courses", courseRepository.findAll());
            model.addAttribute("rooms", roomRepository.findAll());

            return "admin/editSchedule";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/schedule";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при загрузке расписания: " + e.getMessage());
            return "redirect:/admin/schedule";
        }
    }

    @PostMapping("/admin/update-schedule")
    public String updateSchedule(@RequestParam Long id,
                                 @RequestParam String dateTime,
                                 @RequestParam Long courseId,
                                 @RequestParam Long roomId,
                                 RedirectAttributes redirectAttributes) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid schedule Id:" + id));

        LocalDateTime parsedDateTime;
        try {
            parsedDateTime = LocalDateTime.parse(dateTime, DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"));
        } catch (DateTimeParseException e) {
            redirectAttributes.addFlashAttribute("error", "Некорректная дата и время.");
            return "redirect:/admin/edit-schedule?id=" + id;
        }


        int year = parsedDateTime.getYear();
        if (year < 2024 || year > 2025) {
            redirectAttributes.addFlashAttribute("error", "Год должен быть в пределах 2024-2025.");
            return "redirect:/admin/edit-schedule?id=" + id;
        }


        if (scheduleRepository.existsByDateTimeAndCourseIdAndRoomId(parsedDateTime, courseId, roomId) && !schedule.getDateTime().equals(parsedDateTime)) {
            redirectAttributes.addFlashAttribute("error", "Расписание с такими же курсом, кабинетом и временем уже существует.");
            return "redirect:/admin/edit-schedule?id=" + id;
        }

        schedule.setDateTime(parsedDateTime);
        try {
            schedule.setCourse(courseRepository.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + courseId)));
            schedule.setRoom(roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid room Id:" + roomId)));

            scheduleRepository.save(schedule);
            redirectAttributes.addFlashAttribute("success", "Расписание успешно обновлено!");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении расписания: " + e.getMessage());
            return "redirect:/admin/edit-schedule?id=" + id;
        }

        return "redirect:/admin/schedule";
    }

    @GetMapping("/admin/delete-schedule")
    public String deleteSchedule(@RequestParam Long id) {
        scheduleRepository.deleteById(id);
        return "redirect:/admin/schedule";
    }








    @GetMapping("/admin/manage-rooms")
    public String manageRooms(Model model) {
        List<Room> rooms = roomRepository.findAll();
        model.addAttribute("rooms", rooms);
        return "admin/manageRooms";
    }

    @GetMapping("/admin/add-room")
    public String showAddRoomForm(Model model) {
        model.addAttribute("room", new Room());
        return "admin/addRoom";
    }

    @PostMapping("/admin/save-room")
    public String saveRoom(@ModelAttribute Room room, Model model) {
        if (roomRepository.existsByName(room.getName().trim())) {
            model.addAttribute("error", "Комната с таким названием уже существует.");
            return "admin/addRoom";
        }
        roomRepository.save(room);
        return "redirect:/admin/manage-rooms";
    }

    @GetMapping("/admin/edit-room")
    public String showEditRoomForm(@RequestParam Long id, Model model) {
        Room room = roomRepository.findById(id).orElse(null);
        model.addAttribute("room", room);
        return "admin/editRoom";
    }

    @PostMapping("/admin/update-room")
    public String updateRoom(@ModelAttribute Room room, Model model) {
        if (roomRepository.existsByName(room.getName().trim())) {
            model.addAttribute("error", "Комната с таким названием уже существует.");
            return "admin/editRoom";
        }
        roomRepository.save(room);
        return "redirect:/admin/manage-rooms";
    }

    @GetMapping("/admin/delete-room")
    public String deleteRoom(@RequestParam Long id) {
        Optional<Room> room = roomRepository.findById(id);
        if (room.isPresent()) {
            List<Schedule> schedules = scheduleRepository.findByRoomId(id);
            for (Schedule schedule : schedules) {
                List<Grade> grades = gradeRepository.findByCourseId(schedule.getCourse().getId());
            }

            roomRepository.delete(room.get());
        } else {
            System.out.println("Room with ID " + id + " not found.");
            return "redirect:/admin/manage-rooms";
        }
        return "redirect:/admin/manage-rooms";
    }










    @GetMapping("/admin/manage-groups")
    public String manageGroups(Model model) {
        List<Groupss> groups = groupRepository.findAll();
        model.addAttribute("groups", groups);
        return "/admin/manageGroups";
    }

    @GetMapping("/admin/add-group")
    public String showAddGroupForm(Model model) {
        model.addAttribute("group", new Groupss());
        return "/admin/addGroup";
    }

    @PostMapping("/admin/save-group")
    public String saveGroup(@ModelAttribute Groupss group, Model model) {
        if (groupRepository.existsByName(group.getName())) {
            model.addAttribute("error", "Группа с таким названием уже существует.");
            return "/admin/addGroup";
        }
        groupRepository.save(group);
        return "redirect:/admin/manage-groups";
    }

    @GetMapping("/admin/edit-group")
    public String showEditGroupForm(@RequestParam Long id, Model model) {
        Groupss group = groupRepository.findById(id).orElse(null);
        model.addAttribute("group", group);
        return "/admin/editGroup";
    }

    @PostMapping("/admin/update-group")
    public String updateGroup(@ModelAttribute Groupss group, Model model) {
        if (groupRepository.existsByName(group.getName())) {
            model.addAttribute("error", "Группа с таким названием уже существует.");
            model.addAttribute("group", group);
            return "/admin/editGroup";
        }
        groupRepository.save(group);
        return "redirect:/admin/manage-groups";
    }

    @GetMapping("/admin/delete-group")
    public String deleteGroup(@RequestParam Long id, Model model) {
        Groupss group = groupRepository.findById(id).orElse(null);

        if (group != null && "НЕ РАСПРЕДЕЛЕН".equals(group.getName())) {
            model.addAttribute("error", "Эту группу нельзя удалить!");
            return "redirect:/admin/manage-groups";
        }

        groupRepository.deleteById(id);
        return "redirect:/admin/manage-groups";
    }









    @GetMapping("/admin/manage-clients")
    public String manageClients(Model model) {
        List<Client> clients = clientRepository.findAll();
        model.addAttribute("clients", clients);
        return "/admin/manageClients";
    }

    @GetMapping("/admin/add-client")
    public String showAddClientForm(Model model) {
        model.addAttribute("roles", roleRepository.findAll());
        model.addAttribute("groups", groupRepository.findAll());
        return "/admin/addClient";
    }

    @PostMapping("/admin/save-client")
    public String saveClient(@RequestParam String firstName,
                             @RequestParam String lastName,
                             @RequestParam String patronymic,
                             @RequestParam Integer age,
                             @RequestParam Long groupId,
                             @RequestParam String username,
                             @RequestParam String password,
                             @RequestParam String role,
                             Model model) {


        UserTable existingUser = userRepository.findByUsername(username);
        if (existingUser != null) {
            model.addAttribute("message", "Пользователь с таким логином уже существует");
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("groups", groupRepository.findAll());
            return "/admin/addClient";
        }


        if (username.trim().length() < 5 || password.trim().length() < 5) {
            model.addAttribute("message", "Логин и пароль должны содержать минимум 5 символов.");
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("groups", groupRepository.findAll());
            return "/admin/addClient";
        }


        if (age < 16 || age > 65) {
            model.addAttribute("message", "Возраст должен быть от 16 до 65 лет.");
            model.addAttribute("roles", roleRepository.findAll());
            model.addAttribute("groups", groupRepository.findAll());
            return "/admin/addClient";
        }

        String fullName = String.join(" ", firstName, lastName, patronymic);

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

        Groupss group = groupRepository.findById(groupId).orElse(null);
        Client client = new Client();
        client.setFullName(fullName);
        client.setAge(age);
        client.setGroup(group);
        client.setUserTable(user);
        clientRepository.save(client);

        return "redirect:/admin/manage-clients";
    }

    @GetMapping("/admin/edit-client")
    public String showEditClientForm(@RequestParam Long id, Model model) {
        Optional<Client> clientOpt = clientRepository.findById(id);
        if (clientOpt.isPresent()) {
            Client client = clientOpt.get();
            model.addAttribute("client", client);
            model.addAttribute("groups", groupRepository.findAll());
            model.addAttribute("roles", roleRepository.findAll());
            return "/admin/editClient";
        } else {
            model.addAttribute("error", "Клиент не найден");
            return "redirect:/admin/manage-clients";
        }
    }

    @PostMapping("/admin/update-client")
    public String updateClient(@RequestParam Long id,
                               @RequestParam String firstName,
                               @RequestParam String lastName,
                               @RequestParam String patronymic,
                               @RequestParam Integer age,
                               @RequestParam Long groupId,
                               @RequestParam String username,
                               @RequestParam String role,
                               @RequestParam(required = false) String password,
                               Model model) {

        Optional<Client> clientOpt = clientRepository.findById(id);
        if (clientOpt.isEmpty()) {
            model.addAttribute("error", "Клиент не найден");
            return "redirect:/admin/manage-clients?error=client_not_found";
        }

        Client client = clientOpt.get();

        if (age < 16 || age > 65) {
            model.addAttribute("message", "Возраст должен быть от 16 до 65 лет.");
            model.addAttribute("client", client);
            model.addAttribute("groups", groupRepository.findAll());
            model.addAttribute("roles", roleRepository.findAll());
            return "/admin/editClient";
        }

        if (!validateName(firstName) || !validateName(lastName) || !validateName(patronymic)) {
            model.addAttribute("message", "Имя, фамилия и отчество должны содержать минимум 2 буквы и только буквы.");
            model.addAttribute("client", client);
            model.addAttribute("groups", groupRepository.findAll());
            model.addAttribute("roles", roleRepository.findAll());
            return "/admin/editClient";
        }

        client.setFullName(firstName + " " + lastName + " " + patronymic); // Объединение полных имени, фамилии и отчества
        client.setAge(age);

        Optional<Groupss> groupOpt = groupRepository.findById(groupId);
        if (groupOpt.isEmpty()) {
            model.addAttribute("error", "Группа не найдена");
            return "redirect:/admin/manage-clients?error=group_not_found";
        }
        client.setGroup(groupOpt.get());

        UserTable user = client.getUserTable();

        if (userRepository.existsByUsername(username) && !user.getUsername().equals(username)) {
            model.addAttribute("message", "Логин уже существует.");
            model.addAttribute("client", client);
            model.addAttribute("groups", groupRepository.findAll());
            model.addAttribute("roles", roleRepository.findAll());
            return "/admin/editClient";
        }

        user.setUsername(username);

        if (password != null && !password.isEmpty()) {
            user.setPassword(passwordEncoder.encode(password));
        }

        Role userRole = roleRepository.findByName(role.toUpperCase());
        if (userRole == null) {
            userRole = new Role(role.toUpperCase());
            roleRepository.save(userRole);
        }

        Set<Role> roles = new HashSet<>();
        roles.add(userRole);
        user.setRoles(roles);

        userRepository.save(user);
        clientRepository.save(client);

        return "redirect:/admin/manage-clients";
    }

    private boolean validateName(String name) {
        return name != null && name.trim().length() >= 2 && name.matches("[а-яА-ЯёЁa-zA-Z]+");
    }




    @GetMapping("/admin/delete-client")
    public String deleteClient(@RequestParam Long id) {
        clientRepository.deleteById(id);
        return "redirect:/admin/manage-clients";
    }







    @GetMapping("/admin/manage-grades")
    public String manageGrades(Model model) {
        List<Grade> grades = gradeRepository.findAll();
        model.addAttribute("grades", grades);
        return "admin/manageGrades";
    }

    @GetMapping("/admin/add-grade")
    public String showAddGradeForm(Model model) {
        model.addAttribute("grade", new Grade());
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("courses", courseRepository.findAll());
        return "admin/addGrade";
    }

    @PostMapping("/admin/save-grade")
    public String saveGrade(@ModelAttribute Grade grade, RedirectAttributes redirectAttributes) {
        boolean exists = gradeRepository.existsByStudentAndCourse(grade.getStudent(), grade.getCourse());
        if (exists) {
            redirectAttributes.addFlashAttribute("error", "Оценка для этого клиента и курса уже существует.");
            return "redirect:/admin/add-grade";
        }
        gradeRepository.save(grade);
        return "redirect:/admin/manage-grades";
    }

    @GetMapping("/admin/edit-grade")
    public String showEditGradeForm(@RequestParam Long id, Model model) {
        Grade grade = gradeRepository.findById(id).orElse(null);
        model.addAttribute("grade", grade);
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("courses", courseRepository.findAll());
        return "admin/editGrade";
    }

    @PostMapping("/admin/update-grade")
    public String updateGrade(@ModelAttribute Grade grade, RedirectAttributes redirectAttributes) {
        boolean exists = gradeRepository.existsByStudentAndCourse(grade.getStudent(), grade.getCourse());
        if (exists) {
            redirectAttributes.addFlashAttribute("error", "Оценка для этого клиента и курса уже существует.");
            return "redirect:/admin/edit-grade?id=" + grade.getId();
        }
        gradeRepository.save(grade);
        return "redirect:/admin/manage-grades";
    }

    @GetMapping("/admin/delete-grade")
    public String deleteGrade(@RequestParam Long id) {
        gradeRepository.deleteById(id);
        return "redirect:/admin/manage-grades";
    }










    @GetMapping("/admin/manage-courses")
    public String manageCourses(Model model) {
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);
        return "admin/manageCourses";
    }

    @GetMapping("/admin/add-course")
    public String showAddCourseForm(Model model) {
        model.addAttribute("course", new Course());
        model.addAttribute("subjects", subjectRepository.findAll());
        return "admin/addCourse";
    }

    @PostMapping("/admin/save-course")
    public String saveCourse(@ModelAttribute Course course, Model model) {
        String courseName = course.getName().trim().toLowerCase();

        if (courseName.length() < 3 || courseName.matches(".*\\d.*")) {
            model.addAttribute("error", "Название курса должно содержать минимум 3 символа и не должно содержать числа.");
            model.addAttribute("course", course);
            model.addAttribute("subjects", subjectRepository.findAll());
            return "admin/addCourse";
        }

        if (courseRepository.existsByName(courseName)) {
            model.addAttribute("error", "Курс с таким названием уже существует.");
            model.addAttribute("course", course);
            model.addAttribute("subjects", subjectRepository.findAll());
            return "admin/addCourse";
        }

        course.setName(courseName);
        courseRepository.save(course);
        return "redirect:/admin/manage-courses";
    }

    @GetMapping("/admin/edit-course")
    public String showEditCourseForm(@RequestParam Long id, Model model) {
        Course course = courseRepository.findById(id).orElse(null);
        model.addAttribute("course", course);
        model.addAttribute("subjects", subjectRepository.findAll());
        return "admin/editCourse";
    }

    @PostMapping("/admin/update-course")
    public String updateCourse(@ModelAttribute Course course, Model model) {
        String courseName = course.getName().trim().toLowerCase();

        if (courseName.length() < 3 || courseName.matches(".*\\d.*")) {
            model.addAttribute("error", "Название курса должно содержать минимум 3 символа и не должно содержать числа.");
            model.addAttribute("course", course);
            model.addAttribute("subjects", subjectRepository.findAll());
            return "admin/editCourse";
        }

        if (courseRepository.existsByNameAndIdNot(courseName, course.getId())) {
            model.addAttribute("error", "Курс с таким названием уже существует.");
            model.addAttribute("course", course);
            model.addAttribute("subjects", subjectRepository.findAll());
            return "admin/editCourse";
        }

        course.setName(courseName);
        courseRepository.save(course);
        return "redirect:/admin/manage-courses";
    }

    @GetMapping("/admin/delete-course")
    public String deleteCourse(@RequestParam Long id) {
        courseRepository.deleteById(id);
        return "redirect:/admin/manage-courses";
    }






    @GetMapping("/admin/manage-client-courses")
    public String manageClientCourses(Model model) {
        List<ClientCourse> clientCourses = clientCourseRepository.findAll();
        model.addAttribute("clientCourses", clientCourses);
        return "admin/manageClientCourses";
    }

    @GetMapping("/admin/add-client-course")
    public String showAddClientCourseForm(Model model) {
        model.addAttribute("clientCourse", new ClientCourse());
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("courses", courseRepository.findAll());
        return "admin/addClientCourse";
    }


    @PostMapping("/admin/save-client-course")
    public ResponseEntity<String> saveClientCourse(@ModelAttribute ClientCourse clientCourse) {
        Client client = clientRepository.findById(clientCourse.getClient().getId()).orElse(null);
        Course course = courseRepository.findById(clientCourse.getCourse().getId()).orElse(null);

        if (client != null && course != null) {
            boolean exists = clientCourseRepository.existsByClientIdAndCourseId(client.getId(), course.getId());
            if (exists) {
                return ResponseEntity.status(HttpStatus.CONFLICT).body("Этот клиент уже записан на этот курс.");
            }

            clientCourse.setClient(client);
            clientCourse.setCourse(course);
            clientCourseRepository.save(clientCourse);

            return ResponseEntity.ok("Клиент успешно добавлен на курс.");
        }

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Ошибка при добавлении клиента на курс.");
    }


    @GetMapping("/admin/edit-client-course")
    public String showEditClientCourseForm(@RequestParam Long id, Model model) {
        ClientCourse clientCourse = clientCourseRepository.findById(id).orElse(null);
        model.addAttribute("clientCourse", clientCourse);
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("courses", courseRepository.findAll());
        return "admin/editClientCourse";
    }


    @PostMapping("/admin/update-client-course")
    public ResponseEntity<String> updateClientCourse(@ModelAttribute ClientCourse clientCourse) {
        ClientCourse existingClientCourse = clientCourseRepository.findById(clientCourse.getId()).orElse(null);
        if (existingClientCourse == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Запись не найдена.");
        }

        boolean exists = clientCourseRepository.existsByClientIdAndCourseId(clientCourse.getClient().getId(), clientCourse.getCourse().getId());
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Этот клиент уже записан на этот курс.");
        }

        existingClientCourse.setClient(clientCourse.getClient());
        existingClientCourse.setCourse(clientCourse.getCourse());
        clientCourseRepository.save(existingClientCourse);

        return ResponseEntity.ok("Изменения успешно сохранены.");
    }


    @GetMapping("/admin/delete-client-course")
    public String deleteClientCourse(@RequestParam Long id) {
        clientCourseRepository.deleteById(id);
        return "redirect:/admin/manage-client-courses";
    }


}
