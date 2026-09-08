package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.BestowCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "BNG", collectorNumber = "75")
public class HeraldOfTorment extends Card {

    public HeraldOfTorment() {
        addCastingOption(new BestowCast("{3}{B}{B}"));

        addEffect(EffectSlot.UPKEEP_TRIGGERED, new LoseLifeEffect(1));

        target(TargetFilters.creature())
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(
                        3, 3, Set.of(Keyword.FLYING), GrantScope.ENCHANTED_CREATURE));
    }
}
