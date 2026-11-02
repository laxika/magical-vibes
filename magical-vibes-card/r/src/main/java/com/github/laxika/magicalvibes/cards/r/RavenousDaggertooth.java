package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "202")
public class RavenousDaggertooth extends Card {

    public RavenousDaggertooth() {
        // Enrage — Whenever this creature is dealt damage, you gain 2 life.
        addTrigger(Triggers.dealtDamage(), new GainLifeEffect(2));
    }
}
