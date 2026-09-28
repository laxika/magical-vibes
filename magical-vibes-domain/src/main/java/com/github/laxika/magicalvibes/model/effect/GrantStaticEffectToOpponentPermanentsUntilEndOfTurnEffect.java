package com.github.laxika.magicalvibes.model.effect;

/**
 * Dynamically grants a static effect to permanents controlled by the opponents of the resolving
 * effect's controller until end of turn, including permanents entering later that turn.
 *
 * @param staticEffect the static effect granted to opponent permanents
 */
public record GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffect(CardEffect staticEffect)
        implements CardEffect {
}
