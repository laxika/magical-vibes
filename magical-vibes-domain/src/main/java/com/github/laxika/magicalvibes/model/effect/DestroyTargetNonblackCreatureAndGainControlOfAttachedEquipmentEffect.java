package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.filter.PermanentColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import java.util.Set;

/** Destroys a target nonblack creature without regeneration, then gains control of its Equipment. */
public record DestroyTargetNonblackCreatureAndGainControlOfAttachedEquipmentEffect()
        implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(
                TargetPredicates.creature(),
                new PermanentNotPredicate(new PermanentColorInPredicate(Set.of(CardColor.BLACK))));
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
