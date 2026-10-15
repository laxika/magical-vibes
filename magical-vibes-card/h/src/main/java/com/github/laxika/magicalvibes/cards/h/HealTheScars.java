package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TargetToughness;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LRW", collectorNumber = "217")
public class HealTheScars extends Card {

    public HealTheScars() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new RegenerateEffect(true))
                .addEffect(EffectSlot.SPELL, new GainLifeEffect(new TargetToughness()));
    }
}
