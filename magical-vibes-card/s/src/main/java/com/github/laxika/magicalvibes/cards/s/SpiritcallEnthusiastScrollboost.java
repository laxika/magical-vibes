package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;

@CardRegistration(set = "SOS", collectorNumber = "33")
public class SpiritcallEnthusiastScrollboost extends Card {

    public SpiritcallEnthusiastScrollboost() {
        setBackFaceCard(new Scrollboost());

        // Whenever one or more tokens you control enter, this creature becomes prepared.
        addEffect(EffectSlot.ON_ALLY_TOKEN_ENTERS_BATTLEFIELD, new BecomePreparedEffect());
    }

    @Override
    public String getBackFaceClassName() {
        return "Scrollboost";
    }
}
