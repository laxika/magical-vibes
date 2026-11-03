package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "10")
public class DoomskarOracle extends Card {

    public DoomskarOracle() {
        // Whenever you cast your second spell each turn, you gain 2 life.
        addTrigger(Triggers.youCastSpell().nthThisTurn(2), new GainLifeEffect(2));
        addCastingOption(new ForetellCast("{W}"));
    }
}
