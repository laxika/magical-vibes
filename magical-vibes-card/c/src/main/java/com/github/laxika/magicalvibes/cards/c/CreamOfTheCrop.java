package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentPower;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOR", collectorNumber = "117")
@CardRegistration(set = "ECC", collectorNumber = "102")
public class CreamOfTheCrop extends Card {

    public CreamOfTheCrop() {
        // Whenever a creature you control enters, you may look at the top X cards of your library,
        // where X is that creature's power. If you do, put one of those cards on top of your library
        // and the rest on the bottom of your library in any order.
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.ANOTHER_YOU_CONTROL),
                new MayEffect(LookAtTopCardsEffect.putOneOnTopRestOnBottom(new TriggeringPermanentPower()),
                        "Look at the top X cards of your library, where X is that creature's power?"));
    }
}
