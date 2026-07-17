package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

/**
 * Destroys the targeted Wall (it can't be regenerated), then deals damage equal to that Wall's
 * mana value to the Wall's controller. The mana value and controller are snapshotted before the
 * destruction. Used by Word of Blasting.
 */
public record DestroyTargetWallDealManaValueDamageToControllerEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetCategory.PERMANENT, new PermanentHasSubtypePredicate(CardSubtype.WALL));
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
