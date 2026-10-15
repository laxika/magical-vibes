package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ODY", collectorNumber = "156")
public class PatriarchsDesire extends Card {

    public PatriarchsDesire() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(-2, -2, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new ConditionalEffect(
                        new GraveyardCardThreshold(7, null),
                        new StaticBoostEffect(2, -2, GrantScope.ENCHANTED_CREATURE)));
    }
}
