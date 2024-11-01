package org.example.up_itog.controllers;

import jakarta.validation.constraints.NotNull;
import org.example.up_itog.models.*;
import org.example.up_itog.repositories.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/teacher")
public class TeacherController {

    @Autowired
    private ScheduleRepository scheduleRepository;

    @Autowired
    private RoomRepository roomRepository;

    @Autowired
    private ClientRepository clientRepository;

    @Autowired
    private ClientCourseRepository clientCourseRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CourseRepository courseRepository;

    @Autowired
    private GradeRepository gradeRepository;

    @GetMapping("/home")
    public String teacherHome(Model model, @AuthenticationPrincipal UserTable user) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = ((User) auth.getPrincipal()).getUsername();
        UserTable currentUser = userRepository.findByUsername(username);
        model.addAttribute("username", clientRepository.findByUserTable(currentUser));
        return "/teacher/teacherHome";
    }



    @GetMapping("/courses")
    public String getStudentsByCourse(Authentication authentication, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = ((User) auth.getPrincipal()).getUsername();

        UserTable currentUser = userRepository.findByUsername(username);
        if (currentUser == null) {
            throw new UsernameNotFoundException("User not found");
        }


        Client client = clientRepository.findByUserTable(currentUser);
        if (client == null) {
            throw new IllegalStateException("No client associated with this user");
        }

        List<Course> courses = client.getClientCourses().stream()
                .map(ClientCourse::getCourse)
                .collect(Collectors.toList());

        model.addAttribute("courses", courses);

        return "teacher/studentCourses";
    }

    @GetMapping("/courses/add")
    public String showAddClientCourseForm(Model model, @AuthenticationPrincipal UserTable user) {
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("courses", courses);

        List<Client> clients = clientRepository.findAll();
        model.addAttribute("clients", clients);

        return "teacher/addClientCourse";
    }


    @PostMapping("/courses/add")
    public String addClientCourse(@RequestParam Long courseId, @RequestParam Long clientId, @AuthenticationPrincipal UserTable user, Model model) {
        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid course Id:" + courseId));
        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid client Id:" + clientId));

        if (clientCourseRepository.existsByClientAndCourse(client, course)) {
            model.addAttribute("error", "Эта запись уже существует.");
            return showAddClientCourseForm(model, user);
        }

        ClientCourse clientCourse = new ClientCourse();
        clientCourse.setCourse(course);
        clientCourse.setClient(client);

        clientCourseRepository.save(clientCourse);
        return "redirect:/teacher/courses";
    }

    @GetMapping("/courses/edit/{id}")
    public String showEditClientCourseForm(@PathVariable Long id, Model model) {
        ClientCourse clientCourse = clientCourseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ClientCourse Id:" + id));

        List<Client> clients = clientRepository.findAll();
        List<Course> courses = courseRepository.findAll();
        model.addAttribute("clientCourse", clientCourse);
        model.addAttribute("clients", clients);
        model.addAttribute("courses", courses);

        return "teacher/editClientCourse";
    }

    @PostMapping("/courses/edit")
    public String updateClientCourse(@RequestParam Long id, @RequestParam Long clientId, @RequestParam Long courseId) {
        ClientCourse clientCourse = clientCourseRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid ClientCourse Id:" + id));

        Client client = clientRepository.findById(clientId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Client Id:" + clientId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Course Id:" + courseId));

        if (clientCourseRepository.existsByClientAndCourse(client, course)) {
            return "redirect:/teacher/courses?error=duplicate_entry";
        }

        clientCourse.setClient(client);
        clientCourse.setCourse(course);
        clientCourseRepository.save(clientCourse);

        return "redirect:/teacher/courses";
    }


    @GetMapping("/courses/delete/{id}")
    public String deleteClientCourse(@PathVariable Long id) {
        clientCourseRepository.deleteById(id);
        return "redirect:/teacher/courses";
    }







    @GetMapping("/grades/manage")
    public String manageGrades(Authentication authentication, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = ((User) auth.getPrincipal()).getUsername();

        UserTable currentUser = userRepository.findByUsername(username);
        if (currentUser == null) {
            throw new UsernameNotFoundException("User not found");
        }

        Client client = clientRepository.findByUserTable(currentUser);
        if (client == null) {
            throw new IllegalStateException("No client associated with this user");
        }

        List<ClientCourse> clientCourses = clientCourseRepository.findByClient(client);
        List<Grade> grades = new ArrayList<>();

        for (ClientCourse clientCourse : clientCourses) {
            grades.addAll(gradeRepository.findByCourse(clientCourse.getCourse()));
        }

        model.addAttribute("grades", grades);
        model.addAttribute("clientCourses", clientCourses);
        return "teacher/manageGrades";
    }


    @GetMapping("/grades/add")
    public String showAddGradeForm(Model model) {
        model.addAttribute("clients", clientRepository.findAll());
        model.addAttribute("courses", courseRepository.findAll());
        return "teacher/addGrade";
    }


    @PostMapping("/grades/add")
    public String addGrade(@RequestParam Long studentId, @RequestParam Long courseId, @RequestParam int score, Model model) {
        if (score < 0 || score > 100) {
            model.addAttribute("error", "Оценка должна быть между 0 и 100");
            return showAddGradeForm(model);
        }

        Client student = clientRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Неверный идентификатор студента:" + studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Неверный идентификатор курса:" + courseId));

        if (gradeRepository.existsByStudentAndCourse(student, course)) {
            model.addAttribute("error", "У этого студента уже есть оценка за этот курс");
            return showAddGradeForm(model);
        }

        Grade grade = new Grade();
        grade.setStudent(student);
        grade.setCourse(course);
        grade.setScore(score);

        gradeRepository.save(grade);
        return "redirect:/teacher/grades/manage";
    }

    @GetMapping("/grades/edit/{id}")
    public String showEditGradeForm(@PathVariable Long id, Model model) {
        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный идентификатор оценки:" + id));

        List<Client> clients = clientRepository.findAll();
        List<Course> courses = courseRepository.findAll();

        model.addAttribute("grade", grade);
        model.addAttribute("clients", clients);
        model.addAttribute("courses", courses);

        return "teacher/editGrade";
    }


    @PostMapping("/grades/edit")
    public String updateGrade(@RequestParam Long id, @RequestParam Long studentId, @RequestParam Long courseId, @RequestParam int score, Model model) {
        if (score < 0 || score > 100) {
            model.addAttribute("error", "Оценка должна быть между 0 и 100");
            return showEditGradeForm(id, model);
        }

        Grade grade = gradeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Неверный идентификатор оценки:" + id));

        Client student = clientRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("Неверный идентификатор студента:" + studentId));

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Неверный идентификатор курса:" + courseId));

        if (gradeRepository.existsByStudentAndCourseAndIdNot(student, course, id)) {
            model.addAttribute("error", "У этого студента уже есть оценка за этот курс");
            return showEditGradeForm(id, model);
        }

        grade.setStudent(student);
        grade.setCourse(course);
        grade.setScore(score);
        gradeRepository.save(grade);

        return "redirect:/teacher/grades/manage";
    }

    @GetMapping("/grades/delete/{id}")
    public String deleteGrade(@PathVariable Long id) {
        gradeRepository.deleteById(id);
        return "redirect:/teacher/grades/manage";
    }








    @GetMapping("/schedule/manage")
    public String manageSchedule(Authentication authentication, Model model) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = ((User) auth.getPrincipal()).getUsername();

        UserTable currentUser = userRepository.findByUsername(username);
        if (currentUser == null) {
            throw new UsernameNotFoundException("User not found");
        }

        Client client = clientRepository.findByUserTable(currentUser);
        if (client == null) {
            throw new IllegalStateException("No client associated with this user");
        }

        List<ClientCourse> clientCourses = clientCourseRepository.findByClient(client);
        List<Course> courses = clientCourses.stream()
                .map(ClientCourse::getCourse)
                .collect(Collectors.toList());

        List<Schedule> schedules = new ArrayList<>();
        for (Course course : courses) {
            schedules.addAll(scheduleRepository.findByCourse(course));
        }

        model.addAttribute("schedules", schedules);
        return "teacher/manageSchedule";
    }

    @GetMapping("/schedule/add")
    public String showAddScheduleForm(Model model) {
        List<Course> courses = courseRepository.findAll();
        List<Room> rooms = roomRepository.findAll();

        model.addAttribute("courses", courses);
        model.addAttribute("rooms", rooms);
        model.addAttribute("schedule", new Schedule());
        return "teacher/addSchedule";
    }

    @PostMapping("/schedule/add")
    public String addSchedule(@RequestParam Long courseId,
                              @RequestParam Long roomId,
                              @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
                              RedirectAttributes redirectAttributes) {

        if (dateTime.getYear() < 2024 || dateTime.getYear() > 2025) {
            redirectAttributes.addFlashAttribute("error", "Дата должна быть в пределах 2024-2025 годов.");
            return "redirect:/teacher/schedule/add";
        }

        Course course = courseRepository.findById(courseId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Course Id: " + courseId));
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("Invalid Room Id: " + roomId));

        if (scheduleRepository.existsByDateTimeAndCourseAndRoom(dateTime, course, room)) {
            redirectAttributes.addFlashAttribute("error", "Запись с таким временем, курсом и комнатой уже существует.");
            return "redirect:/teacher/schedule/add";
        }

        Schedule schedule = new Schedule();
        schedule.setCourse(course);
        schedule.setRoom(room);
        schedule.setDateTime(dateTime);
        scheduleRepository.save(schedule);

        redirectAttributes.addFlashAttribute("success", "Расписание успешно добавлено!");
        return "redirect:/teacher/schedule/manage";
    }

    @GetMapping("/schedule/edit/{id}")
    public String showEditScheduleForm(@PathVariable Long id, Model model, RedirectAttributes redirectAttributes) {
        try {
            Schedule schedule = scheduleRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Schedule Id: " + id));

            List<Course> courses = courseRepository.findAll();
            List<Room> rooms = roomRepository.findAll();

            model.addAttribute("schedule", schedule);
            model.addAttribute("courses", courses);
            model.addAttribute("rooms", rooms);
            return "teacher/editSchedule";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/teacher/schedule/manage";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при загрузке расписания: " + e.getMessage());
            return "redirect:/teacher/schedule/manage";
        }
    }

    @PostMapping("/schedule/edit")
    public String updateSchedule(@RequestParam Long id,
                                 @RequestParam Long courseId,
                                 @RequestParam Long roomId,
                                 @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateTime,
                                 RedirectAttributes redirectAttributes) {
        try {
            Schedule schedule = scheduleRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Schedule Id: " + id));

            // Валидация даты
            if (dateTime.getYear() < 2024 || dateTime.getYear() > 2025) {
                redirectAttributes.addFlashAttribute("error", "Дата должна быть в пределах 2024-2025 годов.");
                return "redirect:/teacher/schedule/edit/" + id;
            }

            Course course = courseRepository.findById(courseId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Course Id: " + courseId));
            Room room = roomRepository.findById(roomId)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid Room Id: " + roomId));


            if (scheduleRepository.existsByDateTimeAndCourseAndRoom(dateTime, course, room) &&
                    !schedule.getId().equals(id)) {
                redirectAttributes.addFlashAttribute("error", "Запись с таким временем, курсом и комнатой уже существует.");
                return "redirect:/teacher/schedule/edit/" + id;
            }


            schedule.setCourse(course);
            schedule.setRoom(room);
            schedule.setDateTime(dateTime);
            scheduleRepository.save(schedule);

            redirectAttributes.addFlashAttribute("success", "Расписание успешно обновлено!");
            return "redirect:/teacher/schedule/manage";
        } catch (IllegalArgumentException e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/teacher/schedule/edit/" + id;
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Ошибка при обновлении расписания: " + e.getMessage());
            return "redirect:/teacher/schedule/edit/" + id;
        }
    }

    @GetMapping("/schedule/delete/{id}")
    public String deleteSchedule(@PathVariable Long id) {
        scheduleRepository.deleteById(id);
        return "redirect:/teacher/schedule/manage";
    }




}
