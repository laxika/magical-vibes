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

@CardRegistration(set = "E02", collectorNumber = "37")
@CardRegistration(set = "GN3", collectorNumber = "111")
@CardRegistration(set = "CMD", collectorNumber = "178")
public class VowOfWildness extends Card {

    public VowOfWildness() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC,
                        new StaticBoostEffect(3, 3, Set.of(Keyword.TRAMPLE), GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC, new EnchantedCreatureCantAttackControllerEffect());
    }
}
