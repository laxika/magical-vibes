package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "35")
public class LuminousWake extends Card {

    public LuminousWake() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.ON_ATTACK, new GainLifeEffect(4))
                .addEffect(EffectSlot.ON_BLOCK, new GainLifeEffect(4));
    }
}
