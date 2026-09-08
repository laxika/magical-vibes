package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

/**
 * Creates an indefinite replacement effect on one targeted land with the given basic land type.
 * Only mana of {@link #fromColor()} is replaced; other mana types the land could produce are
 * unaffected.
 */
public record ReplaceTargetLandManaWithColorEffect(
        CardSubtype landSubtype,
        ManaColor fromColor,
        ManaColor replacementColor) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.land(),
                new PermanentHasSubtypePredicate(landSubtype));
    }
}
