package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * For each currently attacking creature matching {@code attackerFilter}, its controller may pay
 * {@code manaCost}; if they do not, all combat damage that creature would deal this turn is
 * prevented. The effect creates one independent resolution-time payment choice per creature.
 *
 * @param attackerFilter creatures receiving an independent payment choice
 * @param manaCost mana each matching creature's controller may pay
 */
public record PreventCombatDamageFromAttackingCreaturesUnlessPaysEffect(
        PermanentPredicate attackerFilter,
        String manaCost
) implements CardEffect {
}
