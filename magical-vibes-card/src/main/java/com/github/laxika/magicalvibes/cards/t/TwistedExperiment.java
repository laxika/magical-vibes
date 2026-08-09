package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "UDS", collectorNumber = "74")
public class TwistedExperiment extends Card {

    public TwistedExperiment() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, -1, GrantScope.ENCHANTED_CREATURE));
    }
}
