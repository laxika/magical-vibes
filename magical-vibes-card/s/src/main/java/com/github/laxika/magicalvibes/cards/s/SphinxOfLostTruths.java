package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NotKicked;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "69")
public class SphinxOfLostTruths extends Card {

    public SphinxOfLostTruths() {
        addStaticEffect(new KickerEffect("{1}{U}"));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(3));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new NotKicked(), new DiscardEffect(3, DiscardRecipient.CONTROLLER)));
    }
}
