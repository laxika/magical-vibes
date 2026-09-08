package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesOfChosenSubtypeEffect;

@CardRegistration(set = "ELD", collectorNumber = "111")
public class WitchsVengeance extends Card {

    public WitchsVengeance() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesOfChosenSubtypeEffect(-3, -3));
    }
}
