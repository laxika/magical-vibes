package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a single one-shot delayed trigger: "when you next cast an instant spell, cast a sorcery
 * spell, or activate a loyalty ability this turn, copy that spell or ability twice." Whichever of
 * those events happens first consumes the trigger, so the other kind is no longer copied this turn.
 */
public record CopyNextInstantSorceryOrLoyaltyAbilityThisTurnEffect() implements CardEffect {
}
