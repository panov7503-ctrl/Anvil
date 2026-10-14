package com.example.autoanvil;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AutoAnvilMacro implements ClientModInitializer {

    private static KeyBinding toggleKey;
    private static boolean isActive = false;
    private static int tickCounter = 0;

    @Override
    public void onInitializeClient() {
        // Регистрируем клавишу R с правильным типом KEYCODE для 1.21.4
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autoanvil.toggle",
                InputUtil.Type.KEYCODE,
                GLFW.GLFW_KEY_R,
                "category.autoanvil.general"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Переключение по клавише R
            while (toggleKey.wasPressed()) {
                isActive = !isActive;
                if (isActive) {
                    client.player.sendMessage(Text.literal("§a[AutoAnvil] §fБаг-крафт §aВКЛЮЧЕН"), true);
                } else {
                    client.player.sendMessage(Text.literal("§c[AutoAnvil] §fМакрос §cВЫКЛЮЧЕН"), true);
                }
            }

            // Если макрос активен
            if (isActive) {
                // Проверяем, что открыта наковальня
                if (client.player.currentScreenHandler instanceof AnvilScreenHandler anvilHandler) {
                    tickCounter++;
                    
                    // Срабатывает каждые 10 тиков (полсекунды)
                    if (tickCounter >= 10) {
                        tickCounter = 0;
                        
                        int syncId = anvilHandler.syncId;
                        
                        // Имитируем Shift + Клик по итоговому слоту наковальни (слот 2)
                        client.interactionManager.clickSlot(
                                syncId, 
                                2, 
                                0, 
                                SlotActionType.QUICK_MOVE, 
                                client.player
                        );
                    }
                }
            }
        });
    }
}
