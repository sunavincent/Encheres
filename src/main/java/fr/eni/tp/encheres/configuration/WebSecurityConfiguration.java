package fr.eni.tp.encheres.configuration;



import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.JdbcUserDetailsManager;
import org.springframework.security.provisioning.UserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
public class WebSecurityConfiguration {

    @Bean
    UserDetailsManager userDetailsManager(DataSource dataSource) {
        JdbcUserDetailsManager jdbcUserDetailsManager = new JdbcUserDetailsManager(dataSource);
        jdbcUserDetailsManager.setUsersByUsernameQuery("SELECT pseudo, mot_de_passe, actif FROM UTILISATEURS WHERE pseudo = ?;");
        jdbcUserDetailsManager.setAuthoritiesByUsernameQuery(
                "SELECT pseudo, CASE WHEN administrateur = 1 THEN 'ROLE_ADMIN' ELSE 'ROLE_USER' END FROM UTILISATEURS WHERE pseudo = ?;"
        );//adaptation pour signifier à Spring Security que administrateur correspond à role_admin
        return jdbcUserDetailsManager;
    }

    /**
     *
     * Spring Security veut avoir accès au bcrypt pour pouvoir encoder les passwords
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }



    @Bean
    SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http.authorizeHttpRequests(auth -> {
                    auth
                            .requestMatchers(HttpMethod.GET, "/articles/**").permitAll()
                            .requestMatchers(HttpMethod.GET, "/profile").authenticated()
                            .requestMatchers(HttpMethod.POST, "/profile").authenticated()
                            .requestMatchers(HttpMethod.GET, "/register").permitAll()
                            .requestMatchers(HttpMethod.POST, "/register").permitAll()
                            .requestMatchers(HttpMethod.GET, "/new_sale").authenticated()
                            .requestMatchers(HttpMethod.POST, "/new_sale").authenticated()
                            .requestMatchers(HttpMethod.GET, "/admin").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.GET, "/admin/utilisateur_gestion").hasRole("ADMIN")
                            .requestMatchers(HttpMethod.GET, "/admin/categories_gestion").hasRole("ADMIN")
                            .requestMatchers("/").permitAll()
                            .requestMatchers("/encheres", "/encheres/*").permitAll()
                            .requestMatchers("/css/*").permitAll()
                            .requestMatchers("/images/*").permitAll()
                            .requestMatchers("/js/*").permitAll()
//                            .requestMatchers(("file:///D:/Encheres/uploads/*")).permitAll()
                            .requestMatchers(HttpMethod.GET, "/user/**").authenticated()
                            .anyRequest().authenticated();

                }
        );

        http.formLogin(login -> {
            login.loginPage("/login");
            login.defaultSuccessUrl("/login_success");
            login.permitAll();
        });

        http.logout(logout -> {
            logout.invalidateHttpSession(true);
            logout.deleteCookies("JSESSIONID");
            logout.clearAuthentication(true);
            logout.logoutRequestMatcher(new AntPathRequestMatcher("/logout"));
            logout.logoutSuccessUrl("/encheres");
        });

        return http.build();
    }
}
