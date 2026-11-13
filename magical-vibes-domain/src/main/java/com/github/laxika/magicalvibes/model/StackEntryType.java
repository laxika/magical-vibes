package com.github.laxika.magicalvibes.model;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

public enum StackEntryType {
    CREATURE_SPELL,
    ENCHANTMENT_SPELL,
    TRIGGERED_ABILITY,
    ACTIVATED_ABILITY,
    SORCERY_SPELL,
    INSTANT_SPELL,
    ARTIFACT_SPELL,
    PLANESWALKER_SPELL,
    BATTLE_SPELL;

    /** Activated and triggered abilities on the stack ("target activated or triggered ability"). */
    public static final Set<StackEntryType> ABILITIES =
            Collections.unmodifiableSet(EnumSet.of(ACTIVATED_ABILITY, TRIGGERED_ABILITY));

    /** Triggered abilities on the stack ("target triggered ability"). */
    public static final Set<StackEntryType> TRIGGERED_ABILITIES =
            Collections.unmodifiableSet(EnumSet.of(TRIGGERED_ABILITY));

    /** Every spell and ability on the stack ("target spell or ability"). */
    public static final Set<StackEntryType> SPELLS_AND_ABILITIES =
            Collections.unmodifiableSet(EnumSet.allOf(StackEntryType.class));

    /** {@link #ABILITIES} and {@code spellTypes} ("target instant or sorcery spell, activated ability or ..."). */
    public static Set<StackEntryType> abilitiesAnd(StackEntryType... spellTypes) {
        EnumSet<StackEntryType> types = EnumSet.copyOf(ABILITIES);
        types.addAll(java.util.List.of(spellTypes));
        return Collections.unmodifiableSet(types);
    }

    /** {@link #TRIGGERED_ABILITIES} and {@code spellTypes} ("target instant or sorcery spell, or triggered ability"). */
    public static Set<StackEntryType> triggeredAbilitiesAnd(StackEntryType... spellTypes) {
        EnumSet<StackEntryType> types = EnumSet.copyOf(TRIGGERED_ABILITIES);
        types.addAll(java.util.List.of(spellTypes));
        return Collections.unmodifiableSet(types);
    }

    /** Whether this is an activated or triggered ability rather than a spell. */
    public boolean isAbility() {
        return this == ACTIVATED_ABILITY || this == TRIGGERED_ABILITY;
    }
}
