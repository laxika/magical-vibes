package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleAllOwnCreaturesPowerEffect;

@CardRegistration(set = "SPE", collectorNumber = "13")
public class DoubleTrouble extends Card {

    public DoubleTrouble() {
        addEffect(EffectSlot.SPELL, new DoubleAllOwnCreaturesPowerEffect());
    }
}
