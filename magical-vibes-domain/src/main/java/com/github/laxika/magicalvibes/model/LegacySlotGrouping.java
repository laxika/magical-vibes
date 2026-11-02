package com.github.laxika.magicalvibes.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * How the engine's collectors for a legacy {@link EffectSlot} turn several registrations of the
 * slot on one card into stack objects. {@link Card#addEffect} builds the card's legacy triggered
 * abilities by it. The values come from the Grouping column of {@code effect-slot-inventory.md};
 * slots grouped there as <em>split</em>, <em>varies</em> or <em>none</em> are {@link #EACH}, and
 * their collectors still partition the slot's effects themselves.
 */
enum LegacySlotGrouping {

    /** One triggered ability for each registration, or for each group of simultaneous registrations. */
    EACH,
    /** One triggered ability for every registration in the slot. */
    ALL;

    private static final Set<EffectSlot> ALL_SLOTS = EnumSet.of(
            EffectSlot.ON_SELF_TAPPED_FOR_MANA,
            EffectSlot.SAGA_CHAPTER_I,
            EffectSlot.SAGA_CHAPTER_II,
            EffectSlot.SAGA_CHAPTER_III,
            EffectSlot.SAGA_CHAPTER_IV,
            EffectSlot.SAGA_CHAPTER_V,
            EffectSlot.SAGA_CHAPTER_VI,
            EffectSlot.ON_ALLY_CREATURE_EXPLORES,
            EffectSlot.ON_EXPLOIT,
            EffectSlot.ON_CONTROLLER_CLASHES,
            EffectSlot.ON_PLAYER_LOSES_GAME,
            EffectSlot.ON_CHAMPIONED,
            EffectSlot.ON_SELF_BECOMES_PLOTTED);

    static LegacySlotGrouping of(EffectSlot slot) {
        if (ALL_SLOTS.contains(slot)) {
            return ALL;
        }
        return EACH;
    }
}
