package com.github.laxika.magicalvibes.model.effect;

/**
 * One-shot effect: for the rest of the turn, noncombat damage from sources controlled by the
 * resolving effect's controller gets an additive bonus.
 *
 * <p>As printed on a card {@code amount} is 0 and the bonus is the spell's X; resolving adds a floating effect
 * carrying an instance whose {@code amount} is that bonus, for the turn.
 */
public record BoostControllerNoncombatDamageThisTurnEffect(int amount) implements CardEffect {

    /** The effect as a card prints it: the bonus is the resolving spell's X, which the handler fills in. */
    public BoostControllerNoncombatDamageThisTurnEffect() {
        this(0);
    }
}
