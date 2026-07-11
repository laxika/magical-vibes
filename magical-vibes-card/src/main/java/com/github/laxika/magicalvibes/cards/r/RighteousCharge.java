package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "P02", collectorNumber = "20")
public class RighteousCharge extends Card {

    public RighteousCharge() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(2, 2));
    }
}
