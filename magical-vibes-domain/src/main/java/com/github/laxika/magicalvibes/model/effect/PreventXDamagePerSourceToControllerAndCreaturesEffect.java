package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * Static effect that prevents X damage from each matching source to the source permanent's
 * controller and creatures that player controls.
 *
 * @param amount the amount prevented from each matching damage event
 * @param creatureSourcesOnly whether only creature sources match
 * @param combatOnly whether only combat damage matches
 */
public record PreventXDamagePerSourceToControllerAndCreaturesEffect(
        DynamicAmount amount,
        boolean creatureSourcesOnly,
        boolean combatOnly
) implements CardEffect {
}
