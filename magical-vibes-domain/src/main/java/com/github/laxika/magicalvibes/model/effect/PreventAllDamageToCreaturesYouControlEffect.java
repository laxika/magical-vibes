package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: prevent damage that would be dealt to matching creatures you control (e.g. Emmara Tandris,
 * whose {@code filter} is {@code PermanentIsTokenPredicate}).
 * <p>
 * Applies to every creature controlled by the source's controller that matches {@code filter} (evaluated
 * when damage would be dealt, so it covers creatures that appear or change later). Both combat and
 * noncombat damage is prevented, up to {@code damageLimit} damage remaining. Hooked in
 * {@link com.github.laxika.magicalvibes.service.DamagePreventionService#applyCreaturePreventionShield}.
 *
 * @param filter       extra restriction on the protected creatures, or {@code null} for every creature you control
 * @param damageLimit  maximum damage left after this effect is applied; zero means prevent all damage
 */
public record PreventAllDamageToCreaturesYouControlEffect(PermanentPredicate filter, int damageLimit) implements CardEffect {

    public PreventAllDamageToCreaturesYouControlEffect(PermanentPredicate filter) {
        this(filter, 0);
    }
}
