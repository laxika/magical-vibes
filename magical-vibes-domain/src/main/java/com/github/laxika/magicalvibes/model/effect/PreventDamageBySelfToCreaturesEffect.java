package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: prevent all damage that would be dealt by this creature to creatures matching
 * {@code targetFilter}.
 *
 * @param targetFilter additional restriction on the creatures receiving the damage, or {@code null}
 *                    for every creature
 */
public record PreventDamageBySelfToCreaturesEffect(PermanentPredicate targetFilter)
        implements DamagePreventionBySelfEffect {
}
