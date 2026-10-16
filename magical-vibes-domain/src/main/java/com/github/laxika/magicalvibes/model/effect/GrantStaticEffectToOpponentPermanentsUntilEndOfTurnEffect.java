package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants a static effect to the permanents controlled by the opponents of the resolving effect's
 * controller until end of turn. The affected set is the opponent permanents on the battlefield when the
 * effect resolves (CR 611.2c), so permanents entering later that turn are not affected.
 *
 * @param staticEffect the static effect granted to opponent permanents
 */
public record GrantStaticEffectToOpponentPermanentsUntilEndOfTurnEffect(CardEffect staticEffect)
        implements CardEffect {
}
