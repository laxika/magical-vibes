package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMaySacrificeLandPutSourceOnTopEffect;

@CardRegistration(set = "PLC", collectorNumber = "121")
public class ShivanWumpus extends Card {

    public ShivanWumpus() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new AnyPlayerMaySacrificeLandPutSourceOnTopEffect());
    }
}
