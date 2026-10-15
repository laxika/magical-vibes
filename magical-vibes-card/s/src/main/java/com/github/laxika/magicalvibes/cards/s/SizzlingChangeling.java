package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardsMayPlayUntilNextTurnEffect;

@CardRegistration(set = "ECL", collectorNumber = "155")
public class SizzlingChangeling extends Card {

    public SizzlingChangeling() {
        addEffect(EffectSlot.ON_DEATH, new ExileTopCardsMayPlayUntilNextTurnEffect(1));
    }
}
