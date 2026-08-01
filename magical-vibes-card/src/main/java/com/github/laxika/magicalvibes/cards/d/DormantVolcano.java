package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.ForcedCostOrElseEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnMultiplePermanentsToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTappedPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "VIS", collectorNumber = "161")
public class DormantVolcano extends Card {

    private static final PermanentAllOfPredicate UNTAPPED_MOUNTAIN = new PermanentAllOfPredicate(List.of(
            new PermanentHasSubtypePredicate(CardSubtype.MOUNTAIN),
            new PermanentNotPredicate(new PermanentIsTappedPredicate())));

    public DormantVolcano() {
        // This land enters tapped.
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());

        // When this land enters, sacrifice it unless you return an untapped Mountain you control to
        // its owner's hand.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ForcedCostOrElseEffect(
                new ReturnMultiplePermanentsToHandCost(1, UNTAPPED_MOUNTAIN),
                List.of(new SacrificeSelfEffect()),
                true));

        // {T}: Add {C}{R}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaEffect(ManaColor.COLORLESS), new AwardManaEffect(ManaColor.RED)),
                "{T}: Add {C}{R}."
        ));
    }
}
