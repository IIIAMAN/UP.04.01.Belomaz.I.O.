package org.example.up_itog.config;

import org.example.up_itog.models.Groupss;
import org.example.up_itog.models.Role;
import org.example.up_itog.repositories.RoleRepository;
import org.example.up_itog.repositories.GroupRepository;
import org.example.up_itog.services.UserDetailsServiceImpl;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final UserDetailsServiceImpl userDetailsService;
    private final RoleRepository roleRepository;
    private final GroupRepository groupRepository;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService, RoleRepository roleRepository, GroupRepository groupRepository) {
        this.userDetailsService = userDetailsService;
        this.roleRepository = roleRepository;
        this.groupRepository = groupRepository;
    }

    @Bean
    public CommandLineRunner addInitialData() {
        return args -> {

            List<String> roles = List.of("STUDENT", "TEACHER", "ADMIN");
            for (String roleName : roles) {
                if (roleRepository.findByName(roleName) == null) {
                    roleRepository.save(new Role(roleName));
                }
            }


            if (groupRepository.findByName("НЕ РАСПРЕДЕЛЕН") == null) {
                groupRepository.save(new Groupss("НЕ РАСПРЕДЕЛЕН"));
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http.authorizeHttpRequests((requests) -> requests
                        .requestMatchers("/auth/register", "/auth/login", "/css/**").permitAll()
                        .requestMatchers("/student/**").hasRole("STUDENT")
                        .requestMatchers("/teacher/**").hasRole("TEACHER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin((form) -> form
                        .loginPage("/auth/login")
                        .successHandler(authenticationSuccessHandler())
                        .permitAll()
                )
                .logout((logout) -> logout.permitAll())
                .csrf().disable();

        return http.build();
    }

    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            String role = authentication.getAuthorities().iterator().next().getAuthority();

            switch (role) {
                case "ROLE_STUDENT":
                    response.sendRedirect("/student/home");
                    break;
                case "ROLE_TEACHER":
                    response.sendRedirect("/teacher/home");
                    break;
                case "ROLE_ADMIN":
                    response.sendRedirect("/admin/home");
                    break;
                default:
                    response.sendRedirect("/home");
                    break;
            }
        };
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
        AuthenticationManagerBuilder authenticationManagerBuilder =
                http.getSharedObject(AuthenticationManagerBuilder.class);
        authenticationManagerBuilder.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
        return authenticationManagerBuilder.build();
    }
}
