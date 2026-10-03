package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: for the rest of the turn, sources controlled by the spell's controller deal
 * the configured multiple of their damage to opponents and permanents controlled by opponents.
 */
public record MultiplyControllerDamageToOpponentsAndTheirPermanentsThisTurnEffect(int multiplier)
        implements CardEffect {

    public MultiplyControllerDamageToOpponentsAndTheirPermanentsThisTurnEffect {
        if (multiplier < 1) {
            throw new IllegalArgumentException("Damage multiplier must be positive");
        }
    }
}
