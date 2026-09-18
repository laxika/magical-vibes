package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants a static effect to each creature controlled by the resolving effect's controller's
 * opponents until end of turn.
 *
 * @param staticEffect the static effect granted to opposing creatures
 */
public record GrantStaticEffectToOpponentCreaturesUntilEndOfTurnEffect(CardEffect staticEffect)
        implements CardEffect {
}
