package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "PCY", collectorNumber = "133")
public class VitalizingWind extends Card {

    public VitalizingWind() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(7, 7));
    }
}
