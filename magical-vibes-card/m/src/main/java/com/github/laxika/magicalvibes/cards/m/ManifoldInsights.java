package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ManifoldInsightsEffect;

@CardRegistration(set = "C16", collectorNumber = "10")
@CardRegistration(set = "CM2", collectorNumber = "44")
public class ManifoldInsights extends Card {

    public ManifoldInsights() {
        addEffect(EffectSlot.SPELL, new ManifoldInsightsEffect());
    }
}
