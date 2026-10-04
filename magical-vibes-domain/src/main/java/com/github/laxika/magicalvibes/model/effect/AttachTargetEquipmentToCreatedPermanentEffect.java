package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

/** Attaches the target Equipment to the first permanent created by the same resolution. */
public record AttachTargetEquipmentToCreatedPermanentEffect(boolean useSourceEquipment) implements CardEffect {

    public AttachTargetEquipmentToCreatedPermanentEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return useSourceEquipment ? TargetSpec.NONE : TargetSpec.benign(TargetPredicates.permanent(),
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT));
    }
}
