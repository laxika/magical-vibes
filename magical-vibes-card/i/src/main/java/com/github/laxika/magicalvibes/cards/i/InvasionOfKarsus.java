package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.r.RefractionElemental;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;

@CardRegistration(set = "MOM", collectorNumber = "146")
public class InvasionOfKarsus extends Card {

    public InvasionOfKarsus() {
        setBackFaceCard(new RefractionElemental());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new MassDamageEffect(3, false, false, true, null));
    }

    @Override
    public String getBackFaceClassName() {
        return "RefractionElemental";
    }
}
