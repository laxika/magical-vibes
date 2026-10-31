package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.BestowCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureCantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "JOU", collectorNumber = "24")
public class SightlessBrawler extends Card {

    public SightlessBrawler() {
        addCastingOption(new BestowCast("{4}{W}"));
        addStaticEffect(new CantAttackOrBlockAloneEffect(false));

        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(3, 2, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new EnchantedCreatureCantAttackOrBlockAloneEffect(false));
    }
}
