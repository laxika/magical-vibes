package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "KLD", collectorNumber = "80")
public class EssenceExtraction extends Card {

    public EssenceExtraction() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(3));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(3));
    }
}
