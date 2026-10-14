package com.example.autoanvil;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.AnvilScreenHandler;
import net.minecraft.screen.slot.SlotActionType;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

public class AutoAnvilMacro implements ClientModInitializer {

    private static KeyBinding toggleKey;
    private static boolean isActive = false;
    private static int tickCounter = 0;
    private static int macroStep = 0;

    @Override
    public void onInitializeClient() {
        toggleKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
                "key.autoanvil.toggle",
                InputUtil.Type.KEY_SYM,
                GLFW.GLFW_KEY_R,
                "category.autoanvil.general"
        ));

        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (client.player == null) return;

            // Включение/выключение по клавише R
            while (toggleKey.wasPressed()) {
                isActive = !isActive;
                if (isActive) {
                    client.player.sendMessage(Text.literal("§a[AutoAnvil] §fАвто-алхимик §aВКЛЮЧЕН"), true);
                    macroStep = 0;
                } else {
                    client.player.sendMessage(Text.literal("§c[AutoAnvil] §fМакрос §cВЫКЛЮЧЕН"), true);
                }
            }

            // Основной цикл работы макроса
            if (isActive) {
                tickCounter++;
                
                // Шаг выполняется каждые 12 тиков (около 0.6 секунды), чтобы сервер успевал принимать пакеты
                if (tickCounter >= 12) {
                    tickCounter = 0;

                    // Проверяем, открыта ли наковальня
                    if (!(client.player.currentScreenHandler instanceof AnvilScreenHandler)) {
                        client.player.sendMessage(Text.literal("§e[AutoAnvil] §cОткройте наковальню!"), true);
                        isActive = false;
                        return;
                    }

                    int syncId = client.player.currentScreenHandler.syncId;
                    AnvilScreenHandler handler = (AnvilScreenHandler) client.player.currentScreenHandler;

                    switch (macroStep) {
                        case 0:
                            // Шаг 1: Ищем зелья в инвентаре и кидаем в первый слот наковальни (слот 0)
                            if (findAndMoveItem(client, syncId, 0)) {
                                client.player.sendMessage(Text.literal("§7[AutoAnvil] Первый компонент загружен"), true);
                                macroStep = 1;
                            } else {
                                client.player.sendMessage(Text.literal("§c[AutoAnvil] Не найдены нужные зелья в инвентаре!"), true);
                                isActive = false;
                            }
                            break;

                        case 1:
                            // Шаг 2: Ищем второй компонент и кидаем во второй слот наковальни (слот 1)
                            if (findAndMoveItem(client, syncId, 1)) {
                                client.player.sendMessage(Text.literal("§7[AutoAnvil] Второй компонент загружен"), true);
                                macroStep = 2;
                            } else {
                                client.player.sendMessage(Text.literal("§c[AutoAnvil] Нет второго зелья для скрещивания!"), true);
                                isActive = false;
                            }
                            break;

                        case 2:
                            // Шаг 3: Совершаем баг-клик по результату (слот 2) с QUICK_MOVE (Shift + Клик)
                            client.interactionManager.clickSlot(syncId, 2, 0, SlotActionType.QUICK_MOVE, client.player);
                            client.player.sendMessage(Text.literal("§a[AutoAnvil] §fКрафт/Совмещение выполнено!"), true);
                            
                            // Возвращаемся к первому шагу для обработки следующих стаков
                            macroStep = 0;
                            break;
                    }
                }
            }
        });
    }

    // Вспомогательный метод для поиска зелья в инвентаре и перемещения в слот наковальни
    private static boolean findAndMoveItem(MinecraftClient client, int syncId, int targetSlot) {
        if (client.player == null) return false;

        // Перебираем слоты инвентаря игрока (обычно с 9 по 44 — это основной инвентарь и хотбар)
        for (int i = 9; i < 45; i++) {
            ItemStack stack = client.player.playerScreenHandler.slots.get(i).getStack();
            
            // Проверяем, что это зелье (в дальнейшем здесь можно добавить строгую проверку эффектов)
            if (!stack.isEmpty() && (stack.isOf(Items.POTION) || stack.isOf(Items.SPLASH_POTION) || stack.isOf(Items.LINGERING_POTION))) {
                
                // Берем предмет из инвентаря левой кнопкой мыши
                client.interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, client.player);
                // Кликаем по целевому слоту наковальни (0 или 1), чтобы положить его туда
                client.interactionManager.clickSlot(syncId, targetSlot, 0, SlotActionType.PICKUP, client.player);
                // Возвращаем остаток «в руку» обратно в инвентарь, если что-то осталось
                client.interactionManager.clickSlot(syncId, i, 0, SlotActionType.PICKUP, client.player);
                
                return true;
            }
        }
        return false;
    }
}
