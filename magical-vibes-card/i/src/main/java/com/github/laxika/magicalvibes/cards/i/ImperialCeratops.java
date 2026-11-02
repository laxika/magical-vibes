package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "RIX", collectorNumber = "10")
public class ImperialCeratops extends Card {

    public ImperialCeratops() {
        // Enrage — Whenever this creature is dealt damage, you gain 2 life.
        addTrigger(Triggers.dealtDamage(), new GainLifeEffect(2));
    }
}
