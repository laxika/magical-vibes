package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

/**
 * Attaches the targeted Equipment to the permanent that caused the current trigger.
 */
public record AttachTargetEquipmentToTriggeringPermanentEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent(),
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT));
    }
}
