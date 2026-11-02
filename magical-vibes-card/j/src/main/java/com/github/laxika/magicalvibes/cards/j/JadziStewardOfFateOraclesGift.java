package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.o.OraclesGift;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SOS", collectorNumber = "55")
public class JadziStewardOfFateOraclesGift extends Card {

    public JadziStewardOfFateOraclesGift() {
        setBackFaceCard(new OraclesGift());

        addStaticEffect(new BecomePreparedEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(2));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DiscardEffect(2, DiscardRecipient.CONTROLLER));
    }

    @Override
    public String getBackFaceClassName() {
        return "OraclesGift";
    }
}
