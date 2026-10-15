package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "RIX", collectorNumber = "10")
public class ImperialCeratops extends Card {

    public ImperialCeratops() {
        // Enrage — Whenever this creature is dealt damage, you gain 2 life.
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new GainLifeEffect(2));
    }
}
