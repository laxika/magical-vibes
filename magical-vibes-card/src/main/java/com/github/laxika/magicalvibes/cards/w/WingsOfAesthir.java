package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "ICE", collectorNumber = "305")
public class WingsOfAesthir extends Card {

    public WingsOfAesthir() {
        // Enchant creature
        target(TargetFilters.creature())
                // Enchanted creature gets +1/+0 and has flying and first strike.
                .addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.ENCHANTED_CREATURE))
                .addEffect(EffectSlot.STATIC,
                        new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.FIRST_STRIKE),
                                GrantScope.ENCHANTED_CREATURE));
    }
}
