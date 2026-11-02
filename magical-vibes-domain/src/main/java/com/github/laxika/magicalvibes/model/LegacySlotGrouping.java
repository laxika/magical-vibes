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
            EffectSlot.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE,
            EffectSlot.ON_SELF_CYCLED,
            EffectSlot.ON_CONTROLLER_SEARCHES_LIBRARY,
            EffectSlot.ON_SELF_DISCARDED,
            EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT,
            EffectSlot.ON_BECOMES_TARGET_OF_AURA_SPELL,
            EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL_ONLY,
            EffectSlot.ON_SELF_BECOMES_MONSTROUS,
            EffectSlot.ON_OPPONENT_SEARCHES_LIBRARY,
            EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
            EffectSlot.SAGA_CHAPTER_I,
            EffectSlot.SAGA_CHAPTER_II,
            EffectSlot.SAGA_CHAPTER_III,
            EffectSlot.SAGA_CHAPTER_IV,
            EffectSlot.SAGA_CHAPTER_V,
            EffectSlot.SAGA_CHAPTER_VI,
            EffectSlot.ON_YOU_PUT_COUNTERS_ON_CREATURE_YOU_DONT_CONTROL,
            EffectSlot.ON_YOU_PUT_COUNTERS_ON_CREATURE,
            EffectSlot.ON_SELF_TRAINS,
            EffectSlot.ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_HUMAN,
            EffectSlot.ON_SELF_REACHES_LEVEL_TWO,
            EffectSlot.ON_SELF_REACHES_LEVEL_THREE,
            EffectSlot.ON_SELF_COUNTERS_PUT,
            EffectSlot.ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_NON_HYDRA_CREATURE,
            EffectSlot.ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_ANOTHER_CREATURE,
            EffectSlot.ON_ALLY_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_ANOTHER_PERMANENT_FIRST_TIME_EACH_TURN,
            EffectSlot.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_PERMANENT,
            EffectSlot.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_CREATURE,
            EffectSlot.ON_PLUS_ONE_PLUS_ONE_COUNTERS_PUT_ON_ALLY_CREATURE,
            EffectSlot.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_ANY_CREATURE,
            EffectSlot.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_OTHER_HERO,
            EffectSlot.ON_YOU_PUT_PLUS_ONE_PLUS_ONE_COUNTERS_ON_ANOTHER_CREATURE,
            EffectSlot.ON_YOU_PUT_COUNTERS_ON_ANOTHER_CREATURE,
            EffectSlot.ON_OPPONENT_PUT_COUNTERS_ON_CREATURE_THEY_CONTROL,
            EffectSlot.ON_ALLY_COUNTER_PUT_ON_CREATURE,
            EffectSlot.ON_SELF_EVOLVES,
            EffectSlot.ON_YOU_PUT_LOYALTY_COUNTERS_ON_PLANESWALKERS,
            EffectSlot.ON_SELF_MINUS_ONE_MINUS_ONE_COUNTERS_PUT,
            EffectSlot.ON_MINUS_ONE_MINUS_ONE_COUNTER_PUT_ON_CREATURE,
            EffectSlot.ON_MINUS_ONE_MINUS_ONE_COUNTERS_PUT_ON_CREATURE,
            EffectSlot.ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTER_ON_CREATURE,
            EffectSlot.ON_YOU_PUT_MINUS_ONE_MINUS_ONE_COUNTERS_ON_CREATURE,
            EffectSlot.ON_ALLY_CREATURE_EXPLORES,
            EffectSlot.ON_EXPLOIT,
            EffectSlot.ON_ANOTHER_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
            EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL,
            EffectSlot.ON_ALLY_CREATURE_OR_CREATURE_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
            EffectSlot.ON_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
            EffectSlot.ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
            EffectSlot.ON_OPPONENT_CREATURE_BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY,
            EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL,
            EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_BACKUP_ABILITY,
            EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_INSTANT_OR_SORCERY,
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
