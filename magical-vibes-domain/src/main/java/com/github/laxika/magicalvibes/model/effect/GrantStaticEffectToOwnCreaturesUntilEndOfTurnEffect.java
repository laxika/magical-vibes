package com.github.laxika.magicalvibes.model.effect;

/**
 * Grants a static effect to creatures controlled by the resolving effect's controller until end
 * of turn. The affected set is evaluated continuously, so creatures that enter later in the
 * turn are included.
 *
 * @param staticEffect the static effect granted to the controller's creatures
 */
public record GrantStaticEffectToOwnCreaturesUntilEndOfTurnEffect(CardEffect staticEffect)
        implements CardEffect {
}
