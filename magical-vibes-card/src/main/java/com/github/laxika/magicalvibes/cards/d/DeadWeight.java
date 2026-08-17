package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ISD", collectorNumber = "96")
@CardRegistration(set = "SOI", collectorNumber = "106")
@CardRegistration(set = "GRN", collectorNumber = "67")
public class DeadWeight extends Card {

    public DeadWeight() {
        target(TargetFilters.creature()).addEffect(EffectSlot.STATIC, new StaticBoostEffect(-2, -2, GrantScope.ENCHANTED_CREATURE));
    }
}
