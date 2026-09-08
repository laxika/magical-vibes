package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "JOU", collectorNumber = "18")
public class NyxFleeceRam extends Card {

    public NyxFleeceRam() {
        // At the beginning of your upkeep, you gain 1 life.
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new GainLifeEffect(1));
    }
}
