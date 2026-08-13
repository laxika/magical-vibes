package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByCreaturesWithPowerLessThanIslandCountEffect;

@CardRegistration(set = "BNG", collectorNumber = "42")
public class KrakenOfTheStraits extends Card {

    public KrakenOfTheStraits() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedByCreaturesWithPowerLessThanIslandCountEffect());
    }
}
