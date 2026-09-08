package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.effect.LockOrUnlockTargetRoomDoorEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.effect.SearchLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "251")
public class KeysToTheHouse extends Card {

    public KeysToTheHouse() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(
                        new SacrificeSelfCost(),
                        new SearchLibraryEffect(CardPredicateUtils.basicLand(), LibrarySearchDestination.HAND)
                ),
                "{1}, {T}, Sacrifice this artifact: Search your library for a basic land card, reveal it, put it into your hand, then shuffle."
        ));

        addActivatedAbility(new ActivatedAbility(
                true,
                "{3}",
                List.of(
                        new SacrificeSelfCost(),
                        new LockOrUnlockTargetRoomDoorEffect()
                ),
                "{3}, {T}, Sacrifice this artifact: Lock or unlock a door of target Room you control. Activate only as a sorcery.",
                roomYouControl(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED
        ));
    }

    private static ControlledPermanentPredicateTargetFilter roomYouControl() {
        return new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.ROOM)
                )),
                "Target must be a Room you control");
    }
}
