package com.github.laxika.magicalvibes.model.effect;

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
 * </ul>
 */
public record AssignCombatDamageWithToughnessEffect(GrantScope scope) implements CardEffect {

    /**
     * Backwards-compatible no-arg constructor defaulting to {@link GrantScope#EQUIPPED_CREATURE}.
     */
    public AssignCombatDamageWithToughnessEffect() {
        this(GrantScope.EQUIPPED_CREATURE);
    }
}
