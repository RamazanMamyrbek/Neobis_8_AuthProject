package com.neobis.projects.authproject.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;

@OpenAPIDefinition(
        info = @Info(
                title = "Java - 8 - Auth Project ",
                description = "Проект предполагает разработку программного решения для веб-сайта и мобильного приложения, предназначенного для повышения эффективности онлайн-репетиторства. Задача на данном этапе - реализовать вход в приложение для авторизованных и еще не зарегистрированных пользователей. \n", version = "1.0.0",
                contact = @Contact(
                        name = "Ramazan Mamyrbek",
                        email = "rama.mamirbek@gmail.com"
                )
        )
)
@SecurityScheme(
        name = "JWT",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class SwaggerConfig {

}
