package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "ROE", collectorNumber = "23")
@CardRegistration(set = "IMA", collectorNumber = "22")
public class GuardDuty extends Card {

    public GuardDuty() {
        // Enchant creature
        target(TargetFilters.creature());
        addStaticEffect(new GrantKeywordEffect(Keyword.DEFENDER, GrantScope.ENCHANTED_CREATURE));
    }
}
