package com.example.autoanvil;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;

public class AutoAnvilMacro implements ModInitializer, ClientModInitializer {
    
    @Override
    public void onInitialize() {
        System.out.println("AutoAnvil: Основной модуль запущен!");
    }

    @Override
    public void onInitializeClient() {
        System.out.println("AutoAnvil: Клиентский модуль запущен!");
        // Позже сюда вы добавите логику самого макроса (нажатия клавиш и т.д.)
    }
}

