package com.github.laxika.magicalvibes.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * How the engine's collectors for a legacy {@link EffectSlot} turn several registrations of the
 * slot on one card into stack objects. {@link Card#addEffect} builds the card's legacy triggered
 * abilities by it. The values come from the Grouping column of {@code effect-slot-inventory.md};
 * slots grouped there as <em>split</em>, <em>varies</em> or <em>none</em> are {@link #EACH}, and
 * their collectors still partition the slot's effects themselves. Not used for {@code STATIC} or
 * {@code COMMAND_ZONE_STATIC}, which hold static abilities.
 */
enum LegacySlotGrouping {

    /** One triggered ability for each registration, or for each group of simultaneous registrations. */
    EACH,
    /** One triggered ability for every registration in the slot. */
    ALL,
    /**
     * One triggered ability for the registrations whose {@link TriggerMode} isn't
     * {@link TriggerMode#INDEPENDENT}, and one for each that is.
     */
    ALL_BUT_INDEPENDENT;

    private static final Set<EffectSlot> ALL_SLOTS = EnumSet.of(
            EffectSlot.ON_TAP,
            EffectSlot.ON_SELF_TAPPED_FOR_MANA,
            EffectSlot.ON_ENCHANTED_PLAYER_CREATURE_ENTERS_BATTLEFIELD,
            EffectSlot.ON_ENCHANTED_PLAYER_LAND_ENTERS_BATTLEFIELD,
            EffectSlot.ON_SACRIFICE,
            EffectSlot.ON_BLOCK,
            EffectSlot.ON_SELF_TIME_COUNTER_REMOVED_FROM_EXILE,
            EffectSlot.ON_SELF_CYCLED,
            EffectSlot.ON_CONTROLLER_SEARCHES_LIBRARY,
            EffectSlot.ON_SELF_DISCARDED,
            EffectSlot.ON_SELF_DISCARDED_BY_OPPONENT,
            EffectSlot.ON_BECOMES_TARGET_OF_AURA_SPELL,
            EffectSlot.ON_BECOMES_TARGET_OF_OPPONENT_SPELL_ONLY,
            EffectSlot.ON_SELF_BECOMES_MONSTROUS,
            EffectSlot.ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_YOU,
            EffectSlot.ON_ENCHANTED_CREATURE_DEALS_DAMAGE,
            EffectSlot.ON_ENCHANTED_CREATURE_DEALS_DAMAGE_TO_CREATURE,
            EffectSlot.ON_EQUIPPED_CREATURE_TRANSFORMS,
            EffectSlot.ON_OPPONENT_SEARCHES_LIBRARY,
            EffectSlot.ON_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
            EffectSlot.COMMAND_ZONE_ON_ALLY_CREATURES_ATTACK,
            EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER,
            EffectSlot.ON_ALLY_CREATURES_ATTACK_PLAYER_OR_PLANESWALKER,
            EffectSlot.SAGA_CHAPTER_I,
            EffectSlot.SAGA_CHAPTER_II,
            EffectSlot.SAGA_CHAPTER_III,
            EffectSlot.SAGA_CHAPTER_IV,
            EffectSlot.SAGA_CHAPTER_V,
            EffectSlot.SAGA_CHAPTER_VI,
            EffectSlot.END_OF_COMBAT_TRIGGERED,
            EffectSlot.EACH_PRECOMBAT_MAIN_TRIGGERED,
            EffectSlot.ON_YOU_PUT_COUNTERS_ON_CREATURE_YOU_DONT_CONTROL,
            EffectSlot.ON_YOU_PUT_COUNTERS_ON_CREATURE,
            EffectSlot.EXILE_ON_ALLY_CREATURE_ATTACKS_UNBLOCKED,
            EffectSlot.EXILE_ON_ALLY_CREATURES_ATTACK,
            EffectSlot.GRAVEYARD_ON_COMBAT_DAMAGE_TO_YOU_OR_YOUR_PLANESWALKER,
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
            EffectSlot.ON_ALLY_CREATURE_ATTACKS_UNBLOCKED,
            EffectSlot.ON_OPPONENT_CREATURES_ATTACK_YOU_UNBLOCKED,
            EffectSlot.ON_ANOTHER_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
            EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_OPPONENT_SPELL,
            EffectSlot.ON_ALLY_CREATURE_OR_CREATURE_SPELL_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
            EffectSlot.ON_ALLY_PERMANENT_BECOMES_TARGET_OF_OPPONENT_SPELL_OR_ABILITY,
            EffectSlot.ON_ANY_CREATURE_BECOMES_TARGET_OF_SPELL_OR_ABILITY,
            EffectSlot.ON_OPPONENT_CREATURE_BECOMES_TARGET_OF_YOUR_SPELL_OR_ABILITY,
            EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_SPELL,
            EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_BACKUP_ABILITY,
            EffectSlot.ON_ALLY_CREATURE_BECOMES_TARGET_OF_INSTANT_OR_SORCERY,
            EffectSlot.ON_TURNED_FACE_UP,
            EffectSlot.ON_CREATURES_ATTACK_YOU,
            EffectSlot.ON_OPPONENT_ATTACKS_PLANESWALKER_YOU_CONTROL,
            EffectSlot.ON_ANY_CREATURE_ATTACKS,
            EffectSlot.GRANT_STORM_TO_INSTANT_OR_SORCERY,
            EffectSlot.GRANT_CASCADE_TO_FIRST_SPELL,
            EffectSlot.GRANT_CASCADE_TO_FIRST_SPELL_USING_TREASURE_MANA,
            EffectSlot.GRANT_CASCADE_TO_INSTANT_OR_SORCERY_FROM_HAND,
            EffectSlot.GRANT_CASCADE_TO_ENCHANTMENT_FROM_HAND,
            EffectSlot.GRANT_CASCADE_TO_SLIVER_SPELL,
            EffectSlot.GRANT_DEMONSTRATE_TO_FIRST_SPELL_FROM_OUTSIDE_HAND,
            EffectSlot.GRANT_CASCADE_TO_HAND_SPELLS_WITH_MANA_VALUE_AT_MOST_OPPONENTS_LIFE_LOST,
            EffectSlot.ON_CONTROLLER_CLASHES,
            EffectSlot.ON_PLAYER_LOSES_GAME,
            EffectSlot.ON_BLOCKS_MULTIPLE_CREATURES,
            EffectSlot.ON_ANY_CREATURE_BLOCKS,
            EffectSlot.ON_CHAMPIONED,
            EffectSlot.ON_ALLY_CREATURES_BECOME_BLOCKED,
            EffectSlot.ON_ANY_CREATURES_BLOCK,
            EffectSlot.ON_ANY_PERMANENT_RETURNED_TO_HAND,
            EffectSlot.ON_SELF_BECOMES_PLOTTED);

    private static final Set<EffectSlot> ALL_BUT_INDEPENDENT_SLOTS = EnumSet.of(
            EffectSlot.ON_ALLY_CREATURES_ATTACK,
            EffectSlot.ON_CREATURE_ATTACKS_YOU,
            EffectSlot.ON_CREATURE_ATTACKS_YOU_DIRECTLY,
            EffectSlot.ON_CREATURE_ATTACKS_DEFENDING_PLAYER);

    static LegacySlotGrouping of(EffectSlot slot) {
        if (ALL_SLOTS.contains(slot)) {
            return ALL;
        }
        if (ALL_BUT_INDEPENDENT_SLOTS.contains(slot)) {
            return ALL_BUT_INDEPENDENT;
        }
        return EACH;
    }
}
