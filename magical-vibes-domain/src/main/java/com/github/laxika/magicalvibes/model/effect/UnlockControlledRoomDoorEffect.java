package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

/**
 * Effect that unlocks a locked door of a Room controlled by its controller, optionally limited to
 * a Room already chosen as a target.
 */
public record UnlockControlledRoomDoorEffect(boolean targeted) implements CardEffect {

    public UnlockControlledRoomDoorEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        if (!targeted) {
            return TargetSpec.NONE;
        }
        return TargetSpec.benign(
                TargetPredicates.permanent(),
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.ROOM),
                        new PermanentControlledBySourceControllerPredicate())));
    }
}
