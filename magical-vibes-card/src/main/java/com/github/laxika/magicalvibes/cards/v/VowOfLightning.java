package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackControllerEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "E02", collectorNumber = "30")
public class VowOfLightning extends Card {

    public VowOfLightning() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new StaticBoostEffect(2, 2, Set.of(Keyword.FIRST_STRIKE), GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackControllerEffect());
    }
}
