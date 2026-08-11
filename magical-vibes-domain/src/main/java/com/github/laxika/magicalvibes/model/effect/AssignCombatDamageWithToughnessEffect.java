package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: creatures assign combat damage equal to their toughness rather than their power.
 *
 * <p>The {@code scope} determines which creatures are affected:
 * <ul>
 *   <li>{@link GrantScope#EQUIPPED_CREATURE} / {@link GrantScope#ENCHANTED_CREATURE} —
 *       only the attached creature; also requires toughness &gt; power (equipment/aura pattern,
 *       e.g. Bark of Doran).</li>
 *   <li>{@link GrantScope#OWN_CREATURES} — all other creatures the controller controls;
 *       always uses toughness regardless of power (e.g. Belligerent Brontodon).</li>
 *   <li>{@link GrantScope#ALL_OWN_CREATURES} — all creatures the controller controls
 *       including the source; always uses toughness regardless of power.</li>
 *   <li>{@link GrantScope#ALL_CREATURES} — every creature on the battlefield regardless of
 *       controller; always uses toughness regardless of power (e.g. Doran, the Siege Tower).</li>
 * </ul>
 */
public record AssignCombatDamageWithToughnessEffect(
        GrantScope scope,
        PermanentPredicate affectedPredicate,
        boolean alwaysUseToughness
)
        implements CardEffect {

    public AssignCombatDamageWithToughnessEffect(GrantScope scope) {
        this(scope, null, false);
    }

    public AssignCombatDamageWithToughnessEffect(GrantScope scope, boolean alwaysUseToughness) {
        this(scope, null, alwaysUseToughness);
    }

    public AssignCombatDamageWithToughnessEffect(GrantScope scope, PermanentPredicate affectedPredicate) {
        this(scope, affectedPredicate, false);
    }

    /**
     * Backwards-compatible no-arg constructor defaulting to {@link GrantScope#EQUIPPED_CREATURE}.
     */
    public AssignCombatDamageWithToughnessEffect() {
        this(GrantScope.EQUIPPED_CREATURE, null, false);
    }
}
