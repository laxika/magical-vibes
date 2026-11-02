package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PIP", collectorNumber = "48")
@CardRegistration(set = "PIP", collectorNumber = "576")
public class RuthlessRadrat extends Card {

    public RuthlessRadrat() {
        addCastingCost(RepeatableAdditionalManaCost.graveyardExile(4));
        // "When this creature enters, if its squad cost was paid, create that many token copies" (CR 702.157a):
        // the squad condition is an intervening-if, so the trigger does not go on the stack without a payment.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ConditionalEffect(new RepeatedAdditionalCostPaid("{0}"),
                        new CreateTokenCopyOfSourceEffect(false, new RepeatedAdditionalCostCount("{0}"))));
    }
}
