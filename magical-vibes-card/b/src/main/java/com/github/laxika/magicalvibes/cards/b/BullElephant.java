package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentToHandCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "VIS", collectorNumber = "101")
@CardRegistration(set = "MGB", collectorNumber = "9")
public class BullElephant extends Card {

    public BullElephant() {
        // When this creature enters, sacrifice it unless you return two Forests you control to
        // their owner's hand.
        addTrigger(Triggers.enters(ObjectRelation.THIS), UnlessPaysEffect.unlessYouPay(
                ReturnPermanentToHandCost.youControl(2,
                        new PermanentHasSubtypePredicate(CardSubtype.FOREST), "Forests you control"),
                new SacrificeSelfEffect()));
    }
}
