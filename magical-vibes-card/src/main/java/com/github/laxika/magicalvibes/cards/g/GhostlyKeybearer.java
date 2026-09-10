package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.UnlockControlledRoomDoorEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "61")
public class GhostlyKeybearer extends Card {

    public GhostlyKeybearer() {
        target(roomYouControl(), 0, 1).addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new UnlockControlledRoomDoorEffect(true));
    }

    private static ControlledPermanentPredicateTargetFilter roomYouControl() {
        return new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.ROOM))),
                "Target must be a Room you control");
    }
}
