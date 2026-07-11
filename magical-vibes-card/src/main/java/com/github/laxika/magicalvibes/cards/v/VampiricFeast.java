package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "POR", collectorNumber = "114")
public class VampiricFeast extends Card {

    public VampiricFeast() {
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(4));
        addEffect(EffectSlot.SPELL, new GainLifeEffect(4));
    }
}
