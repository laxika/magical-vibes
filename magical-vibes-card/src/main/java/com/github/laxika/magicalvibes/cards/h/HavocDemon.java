package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

@CardRegistration(set = "LGN", collectorNumber = "74")
public class HavocDemon extends Card {

    public HavocDemon() {
        addEffect(EffectSlot.ON_DEATH, new BoostAllCreaturesEffect(-5, -5));
    }
}
