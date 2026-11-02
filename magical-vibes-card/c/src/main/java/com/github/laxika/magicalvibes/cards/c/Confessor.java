package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "15")
public class Confessor extends Card {

    public Confessor() {
        addTrigger(Triggers.opponentDiscardsCard(), new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
        addTrigger(Triggers.youDiscardCard(), new MayEffect(new GainLifeEffect(1), "Gain 1 life?"));
    }
}
