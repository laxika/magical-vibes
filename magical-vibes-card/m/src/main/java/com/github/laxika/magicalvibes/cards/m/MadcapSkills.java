package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GTC", collectorNumber = "98")
@CardRegistration(set = "MM3", collectorNumber = "99")
public class MadcapSkills extends Card {

    public MadcapSkills() {
        // Enchant creature
        target(TargetFilters.creature())
                // Enchanted creature gets +3/+0 and has menace.
                .addStaticEffect(new StaticBoostEffect(3, 0, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantKeywordEffect(Keyword.MENACE, GrantScope.ENCHANTED_CREATURE));
    }
}
