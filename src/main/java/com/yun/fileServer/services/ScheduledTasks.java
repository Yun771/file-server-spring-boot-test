package com.yun.fileServer.services;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Date;

@Component
public class ScheduledTasks {

    @Scheduled(cron = "0 * * * * *")
    public void testTask() {
        // TODO: IMPLEMENTAR EL METODO PARA ELIMINAR LOS ARCHIVOS TEMPORALES CADA DIA A LAS 3 AM
        System.out.println(new Date() + " hola");
    }
}

