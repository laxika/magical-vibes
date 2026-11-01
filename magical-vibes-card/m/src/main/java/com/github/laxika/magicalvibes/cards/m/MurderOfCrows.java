package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "70")
@CardRegistration(set = "A25", collectorNumber = "66")
@CardRegistration(set = "SIS", collectorNumber = "19")
@CardRegistration(set = "CMM", collectorNumber = "106")
@CardRegistration(set = "AFC", collectorNumber = "88")
public class MurderOfCrows extends Card {

    public MurderOfCrows() {
        // Whenever another creature dies, you may draw a card. If you do, discard a card.
        addTrigger(Triggers.creatureDies(ObjectRelation.ANOTHER), new MayEffect(
                SequenceEffect.of(new DrawCardEffect(1), new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "Draw a card and discard a card?"
        ));
    }
}
