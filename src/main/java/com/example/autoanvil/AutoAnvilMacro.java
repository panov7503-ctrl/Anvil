package com.example.autoanvil;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AutoAnvilMacro implements ModInitializer, ClientModInitializer {

    private static KeyBinding toggleKey;
    private static boolean isActive = false;
    private static int tickCounter = 0;
    private static int state = 0; // 0 - ожидание, 1 - проверка опыта/бросок, 2 - закладывание зелий, 3 - забор результата

    @Override
    public void onInitialize() {}

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autoanvil.toggle",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_R,
                "category.autoanvil.general"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null || client.interactionManager == null) return;

            // Включение/выключение макроса по клавише R
            while (toggleKey.wasPressed()) {
                isActive = !isActive;
                if (isActive) {
                    client.player.sendMessage(Text.literal("§a[AutoAnvil] §fАвто-крафт зелий §aВКЛЮЧЕН"), true);
                    state = 1;
                } else {
                    client.player.sendMessage(Text.literal("§c[AutoAnvil] §fМакрос §cВЫКЛЮЧЕН"), true);
                    state = 0;
                }
            }

            if (!isActive) return;

            tickCounter++;
            // Задержка между действиями (10 тиков = 0.5 секунды, чтобы сервер успевал обрабатывать пакеты)
            if (tickCounter >= 10) {
                tickCounter = 0;

                int syncId = client.player.currentScreenHandler != null ? client.player.currentScreenHandler.syncId : 0;

                switch (state) {
                    case 1: // Шаг 1: Проверяем опыт (если меньше 3 уровней, кидаем пузырек под себя)
                        if (client.player.experienceLevel < 3) {
                            client.player.sendMessage(Text.literal("§e[AutoAnvil] Мало опыта, кидаем пузырек..."), true);
                            // Здесь можно добавить логику выбора слота с опытом и броска, 
                            // либо макрос ждет, пока вы наберете опыт.
                        }
                        state = 2;
                        break;

                    case 2: // Шаг 2: Если открыта наковальня, раскладываем зелья
                        if (client.player.currentScreenHandler instanceof AnvilScreenHandler) {
                            // Ищем и перекладываем первое зелье в слот 0
                            if (moveItemToSlot(client, syncId, 0)) {
                                // Ищем и перекладываем второе зелье в слот 1
                                moveItemToSlot(client, syncId, 1);
                                state = 3;
                            } else {
                                client.player.sendMessage(Text.literal("§c[AutoAnvil] Зелья в инвентаре закончились!"), true);
                                isActive = false;
                            }
                        } else {
                            client.player.sendMessage(Text.literal("§e[AutoAnvil] Подойдите и откройте наковальню!"), true);
                        }
                        break;

                    case 3: // Шаг 3: Забираем готовое зелье из выходного слота (слот 2)
                        if (client.player.currentScreenHandler instanceof AnvilScreenHandler) {
                            client.interactionManager.clickSlot(syncId, 2, 0, SlotActionType.QUICK_MOVE, client.player);
                            client.player.sendMessage(Text.literal("§a[AutoAnvil] Зелье скрафчено! Повторяем цикл..."), true);
                            state = 2; // Возвращаемся к закладыванию следующей пары
                        } else {
                            state = 2;
                        }
                        break;
                }
            }
        });
    }

    // Метод для поиска зелья в инвентаре и перемещения в слот наковальни
    private static boolean moveItemToSlot(net.minecraft.client.MinecraftClient client, int syncId, int targetSlot) {
        if (client.player == null || client.interactionManager == null) return false;

        for (int i = 9; i < 45; i++) {
            ItemStack stack = client.player.playerScreenHandler.slots.get(i).getStack();
            if (!stack.isEmpty() && stack.isOf(Items.POTION)) {
                client.interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, client.player);
                client.interactionManager.clickSlot(syncId, targetSlot, 0, SlotActionType.PICKUP, client.player);
                client.interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, client.player);
                return true;
            }
        }
        return false;
    }
}
