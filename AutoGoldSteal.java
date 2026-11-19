package com.example.addon.modules;

import meteordevelopment.meteorclient.events.world.TickEvent;
import meteordevelopment.meteorclient.settings.BoolSetting;
import meteordevelopment.meteorclient.settings.IntSetting;
import meteordevelopment.meteorclient.settings.ItemListSetting;
import meteordevelopment.meteorclient.settings.Setting;
import meteordevelopment.meteorclient.settings.SettingGroup;
import meteordevelopment.meteorclient.systems.modules.Categories;
import meteordevelopment.meteorclient.systems.modules.Module;
import meteordevelopment.orbit.EventHandler;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.screen.slot.SlotActionType;

import java.util.List;

public class AutoGoldSteal extends Module {
    private final SettingGroup sgGeneral = settings.getDefaultGroup();

    private final Setting<List<Item>> items = sgGeneral.add(new ItemListSetting.Builder()
        .name("items")
        .description("Danh sach vat pham vang can tu dong nhat tu ruong hoac GUI.")
        .defaultValue(List.of(
            Items.GOLD_INGOT,
            Items.GOLD_NUGGET,
            Items.GOLD_BLOCK,
            Items.RAW_GOLD,
            Items.RAW_GOLD_BLOCK,
            Items.GOLDEN_APPLE,
            Items.ENCHANTED_GOLDEN_APPLE
        ))
        .build()
    );

    private final Setting<Integer> delay = sgGeneral.add(new IntSetting.Builder()
        .name("delay")
        .description("Do tre (tinh bang tick, 20 tick = 1 giay) giua moi lan nhat.")
        .defaultValue(2)
        .min(0)
        .sliderMax(20)
        .build()
    );

    private final Setting<Boolean> closeWhenDone = sgGeneral.add(new BoolSetting.Builder()
        .name("close-when-done")
        .description("Tu dong dong ruong/GUI sau khi da nhat het vang.")
        .defaultValue(false)
        .build()
    );

    private int timer = 0;

    public AutoGoldSteal() {
        super(Categories.Misc, "auto-gold-steal", "Tu dong nhat cac vat pham vang trong ruong hoac giao dien GUI.");
    }

    @Override
    public void onActivate() {
        timer = 0;
    }

    @EventHandler
    private void onTick(TickEvent.Post event) {
        if (mc.player == null || mc.interactionManager == null) return;

        ScreenHandler handler = mc.player.currentScreenHandler;
        // Bo qua khi chi dang mo tui do ca nhan (playerScreenHandler)
        if (handler == null || handler == mc.player.playerScreenHandler) {
            timer = 0;
            return;
        }

        if (timer > 0) {
            timer--;
            return;
        }

        boolean foundAny = false;

        for (int i = 0; i < handler.slots.size(); i++) {
            Slot slot = handler.slots.get(i);

            // Chi kiem tra cac slot thuoc ruong / GUI ben ngoai, bo qua tui do nguoi choi
            if (slot.inventory == mc.player.getInventory()) continue;
            if (!slot.hasStack()) continue;

            ItemStack stack = slot.getStack();
            if (items.get().contains(stack.getItem())) {
                foundAny = true;

                // Kiem tra tui do con cho trong hoac co the gop stack khong
                if (!canFit(stack)) continue;

                mc.interactionManager.clickSlot(
                    handler.syncId,
                    slot.id,
                    0,
                    SlotActionType.QUICK_MOVE,
                    mc.player
                );

                timer = delay.get();
                return;
            }
        }

        if (!foundAny && closeWhenDone.get()) {
            mc.player.closeHandledScreen();
        }
    }

    private boolean canFit(ItemStack target) {
        if (mc.player == null) return false;

        // Kiem tra 36 o tui do chinh (0..35)
        for (int i = 0; i < 36; i++) {
            ItemStack invStack = mc.player.getInventory().getStack(i);
            if (invStack.isEmpty()) return true;
            if (ItemStack.areItemsAndComponentsEqual(invStack, target)
                && invStack.getCount() < invStack.getMaxCount()) {
                return true;
            }
        }
        return false;
    }
}
