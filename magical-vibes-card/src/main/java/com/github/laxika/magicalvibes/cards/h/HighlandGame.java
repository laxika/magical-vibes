package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "M19", collectorNumber = "188")
public class HighlandGame extends Card {

    public HighlandGame() {
        addEffect(EffectSlot.ON_DEATH, new GainLifeEffect(2));
    }
}
