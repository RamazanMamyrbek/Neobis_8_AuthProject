package com.neobis.projects.authproject.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

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
public class SwaggerConfig {

}
