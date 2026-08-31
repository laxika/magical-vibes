package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ONS", collectorNumber = "40")
public class ImprovisedArmor extends Card {

    public ImprovisedArmor() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 5, GrantScope.ENCHANTED_CREATURE));
        addCycling("{3}");
    }
}
