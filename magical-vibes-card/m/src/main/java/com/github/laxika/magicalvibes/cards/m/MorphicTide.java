package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MorphicTideEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OPC2", collectorNumber = "3")
@CardRegistration(set = "OPCA", collectorNumber = "3")
public class MorphicTide extends Card {

    public MorphicTide() {
        addTrigger(Triggers.encounterThis(), new MorphicTideEffect());
    }
}
