package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "GPT", collectorNumber = "49")
@CardRegistration(set = "FRF", collectorNumber = "68")
public class DouseInGloom extends Card {

    public DouseInGloom() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(2));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(2));
    }
}
