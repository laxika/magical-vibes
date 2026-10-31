package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MBS", collectorNumber = "22")
public class CorruptedConscience extends Card {

    public CorruptedConscience() {
        target(TargetFilters.creature())
                .addStaticEffect(new ControlEnchantedCreatureEffect())
                .addStaticEffect(new GrantKeywordEffect(Keyword.INFECT, GrantScope.ENCHANTED_CREATURE));
    }
}
