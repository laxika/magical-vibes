package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants a static effect to permanents controlled by the resolving effect's controller until end
 * of turn. The affected set is evaluated continuously, so permanents that enter later in the turn
 * are included.
 *
 * @param staticEffect the static effect granted to the controller's permanents
 */
public record GrantStaticEffectToOwnPermanentsUntilEndOfTurnEffect(CardEffect staticEffect)
        implements CardEffect {
}
