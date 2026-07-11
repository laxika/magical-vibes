package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "PTK", collectorNumber = "29")
public class VirtuousCharge extends Card {

    public VirtuousCharge() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(1, 1));
    }
}
