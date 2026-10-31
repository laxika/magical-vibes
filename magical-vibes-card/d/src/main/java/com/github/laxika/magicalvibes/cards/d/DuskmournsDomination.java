package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LosesAllAbilitiesEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "DSK", collectorNumber = "50")
public class DuskmournsDomination extends Card {

    public DuskmournsDomination() {
        target(TargetFilters.creature())
                .addStaticEffect(new ControlEnchantedCreatureEffect())
                .addStaticEffect(new StaticBoostEffect(-3, 0, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new LosesAllAbilitiesEffect(GrantScope.ENCHANTED_CREATURE));
    }
}
