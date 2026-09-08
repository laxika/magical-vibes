package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LockOrUnlockTargetRoomDoorEffect;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

@CardRegistration(set = "DSK", collectorNumber = "221")
public class MarinaVendrell extends Card {

    public MarinaVendrell() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new LookAtTopCardsEffect(new Fixed(7), new Fixed(7),
                        new CardTypePredicate(CardType.ENCHANTMENT),
                        LookDestination.BOTTOM_OF_LIBRARY_RANDOM, true));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new LockOrUnlockTargetRoomDoorEffect()),
                "{T}: Lock or unlock a door of target Room you control. Activate only as a sorcery.",
                roomYouControl(),
                null,
                null,
                ActivationTimingRestriction.SORCERY_SPEED));
    }

    private static ControlledPermanentPredicateTargetFilter roomYouControl() {
        return new ControlledPermanentPredicateTargetFilter(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsEnchantmentPredicate(),
                        new PermanentHasSubtypePredicate(CardSubtype.ROOM))),
                "Target must be a Room you control");
    }
}
