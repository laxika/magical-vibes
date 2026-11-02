package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RefractionElemental;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MOM", collectorNumber = "146")
public class InvasionOfKarsus extends Card {

    public InvasionOfKarsus() {
        setBackFaceCard(new RefractionElemental());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new MassDamageEffect(3, false, false, true, null));
    }

    @Override
    public String getBackFaceClassName() {
        return "RefractionElemental";
    }
}
