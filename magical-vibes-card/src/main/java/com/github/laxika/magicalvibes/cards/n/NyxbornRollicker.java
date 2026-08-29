package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.BestowCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "BNG", collectorNumber = "102")
public class NyxbornRollicker extends Card {

    public NyxbornRollicker() {
        addCastingOption(new BestowCast("{1}{R}"));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE));
    }
}
