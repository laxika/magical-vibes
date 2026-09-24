package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the spell that caused the trigger, then digs the caster's library until a nonland card
 * is found. The source deals damage to each opponent equal to the absolute mana-value difference
 * and offers that card for a free cast.
 */
public record ExileTriggeringSpellUntilNonlandDealManaValueDifferenceAndMayCastEffect()
        implements TriggeringSpellManaValueEffect {
}
