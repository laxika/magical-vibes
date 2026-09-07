package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopTenCardsForColorPairsEffect;

@CardRegistration(set = "WAR", collectorNumber = "208")
public class NivMizzetReborn extends Card {

    public NivMizzetReborn() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new RevealTopTenCardsForColorPairsEffect());
    }
}
