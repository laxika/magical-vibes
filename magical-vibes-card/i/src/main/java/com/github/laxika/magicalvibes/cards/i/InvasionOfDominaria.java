package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SerraFaithkeeper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "21")
public class InvasionOfDominaria extends Card {

    public InvasionOfDominaria() {
        setBackFaceCard(new SerraFaithkeeper());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(4));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1));
    }

    @Override
    public String getBackFaceClassName() {
        return "SerraFaithkeeper";
    }
}
