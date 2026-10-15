package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "LRW", collectorNumber = "118")
public class HornetHarasser extends Card {

    public HornetHarasser() {
        addEffect(EffectSlot.ON_DEATH, new BoostTargetCreatureEffect(-2, -2));
    }
}
