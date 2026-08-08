package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect: "Prevent all damage that would be dealt to [creatures] you control." (e.g. Emmara Tandris,
 * whose {@code filter} is {@code PermanentIsTokenPredicate}).
 * <p>
 * Applies to every creature controlled by the source's controller that matches {@code filter} (evaluated
 * when damage would be dealt, so it covers creatures that appear or change later). Both combat and
 * noncombat damage is prevented. Hooked in
 * {@link com.github.laxika.magicalvibes.service.DamagePreventionService#applyCreaturePreventionShield}.
 *
 * @param filter extra restriction on the protected creatures, or {@code null} for every creature you control
 */
public record PreventAllDamageToCreaturesYouControlEffect(PermanentPredicate filter) implements CardEffect {
}
