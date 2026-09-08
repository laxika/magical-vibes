package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Makes the controller of the spell or ability that caused a trigger sacrifice permanents.
 *
 * <p>The triggering spell or ability is carried as an internal stack reference, not as a chosen
 * target. The effect handler delegates the actual sacrifice choice to the common forced-sacrifice
 * implementation.
 */
public record TriggeringSpellControllerSacrificesPermanentsEffect(
        DynamicAmount count, PermanentPredicate filter) implements TriggeringSpellReferencingEffect {

    public TriggeringSpellControllerSacrificesPermanentsEffect(int count, PermanentPredicate filter) {
        this(new Fixed(count), filter);
    }
}
