package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Activated-ability cost that taps {@code count} untapped permanents matching {@code filter}
 * ("Tap five untapped Myr you control"). The count is evaluated when the cost handler is built, so
 * an {@link XValue} amount expresses "Tap X untapped Knights you control" (Aryel, Knight of
 * Windgrace) with the X the player announced at activation.
 *
 * <p>Replaces the former {@code TapXPermanentsCost}.
 *
 * @param count         how many permanents must be tapped
 * @param filter        which permanents qualify
 * @param excludeSource whether the ability's own source permanent is excluded from the choices
 *                      (set this when the ability also has {@code requiresTap = true})
 */
public record TapMultiplePermanentsCost(DynamicAmount count, PermanentPredicate filter, boolean excludeSource)
        implements CostEffect {

    public TapMultiplePermanentsCost(int count, PermanentPredicate filter) {
        this(new Fixed(count), filter, false);
    }

    public TapMultiplePermanentsCost(int count, PermanentPredicate filter, boolean excludeSource) {
        this(new Fixed(count), filter, excludeSource);
    }
}
