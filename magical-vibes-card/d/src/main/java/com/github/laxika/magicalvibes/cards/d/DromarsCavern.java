package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaOfColorsEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "138")
@CardRegistration(set = "DMR", collectorNumber = "246")
public class DromarsCavern extends Card {

    public DromarsCavern() {
        // When this land enters, sacrifice it unless you return a non-Lair land you control to its owner's hand.
        addTrigger(Triggers.enters(ObjectRelation.THIS), UnlessPaysEffect.unlessYouPay(
                ReturnPermanentToHandCost.youControl(new PermanentAllOfPredicate(List.of(
                        new PermanentIsLandPredicate(),
                        new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.LAIR)))),
                        "non-Lair land you control"),
                new SacrificeSelfEffect()));

        // {T}: Add {W}, {U}, or {B}.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new AwardManaOfColorsEffect(List.of(ManaColor.WHITE, ManaColor.BLUE, ManaColor.BLACK))),
                "{T}: Add {W}, {U}, or {B}."
        ));
    }
}
