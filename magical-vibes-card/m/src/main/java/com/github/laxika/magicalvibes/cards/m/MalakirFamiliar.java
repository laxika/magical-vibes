package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BFZ", collectorNumber = "116")
public class MalakirFamiliar extends Card {

    public MalakirFamiliar() {
        addTrigger(Triggers.youGainLife(), BoostEffect.self(1, 1));
    }
}
