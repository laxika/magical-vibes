package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "ORI", collectorNumber = "227")
public class GuardianAutomaton extends Card {

    public GuardianAutomaton() {
        // When this creature dies, you gain 3 life.
        addEffect(EffectSlot.ON_DEATH, new GainLifeEffect(3));
    }
}
