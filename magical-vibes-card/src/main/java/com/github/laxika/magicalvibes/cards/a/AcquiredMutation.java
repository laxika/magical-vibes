package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GoadEquippedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GiveDefendingPlayerRadCountersEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "PIP", collectorNumber = "53")
@CardRegistration(set = "PIP", collectorNumber = "581")
public class AcquiredMutation extends Card {

    public AcquiredMutation() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new GoadEquippedCreatureEffect())
                .addEffect(EffectSlot.ON_ATTACK, new GiveDefendingPlayerRadCountersEffect(2));
    }
}
