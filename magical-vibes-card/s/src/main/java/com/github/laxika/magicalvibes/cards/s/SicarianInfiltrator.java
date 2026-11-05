package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.RepeatedAdditionalCostCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "40K", collectorNumber = "25")
public class SicarianInfiltrator extends Card {

    public SicarianInfiltrator() {
        // Squad {2}
        addCastingCost(new RepeatableAdditionalManaCost(List.of("{2}")));

        // Create a token copy of Sicarian Infiltrator for each time its squad cost was paid.
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new CreateTokenCopyOfSourceEffect(false, new RepeatedAdditionalCostCount("{2}")));

        // Benediction of the Omnissiah — draw a card when this creature enters.
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1));
    }
}
