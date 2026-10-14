package com.example.autoanvil;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AutoAnvilMacro implements ClientModInitializer {

    private static KeyBinding toggleKey;
    private static boolean isActive = false;
    private static int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        // Регистрируем клавишу "R" для включения и выключения макроса
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autoanvil.toggle", // Уникальный ключ
                InputUtil.Type.KEY_SYM,
                GLFW.GLFW_KEY_R,        // Клавиша R
                "category.autoanvil.general"
        ));

        // Отслеживаем каждый такт игры (20 тактов = 1 секунда)
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Проверяем, нажали ли мы клавишу R
            while (toggleKey.wasPressed()) {
                isActive = !isActive;
                if (isActive) {
                    client.player.sendMessage(Text.literal("§a[AutoAnvil] §fМакрос §аВКЛЮЧЕН"), true);
                } else {
                    client.player.sendMessage(Text.literal("§c[AutoAnvil] §fМакрос §cВЫКЛЮЧЕН"), true);
                }
            }

            // Если макрос активен, выполняем логику
            if (isActive) {
                tickCounter++;
                
                // Каждые 20 тактов (1 секунда) выполняем действие
                if (tickCounter >= 20) {
                    tickCounter = 0;
                    
                    // Здесь будет логика проверки наковальни и кликов мышкой
                    // Например, проверка: client.player.currentScreenhandler
                }
            }
        });
    }
}
