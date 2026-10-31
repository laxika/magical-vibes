package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "LRW", collectorNumber = "80")
public class ProtectiveBubble extends Card {

    public ProtectiveBubble() {
        // Enchant creature
        target(TargetFilters.creature());
        addStaticEffect(new CantBeBlockedEffect());
        addStaticEffect(new GrantKeywordEffect(Keyword.SHROUD, GrantScope.ENCHANTED_CREATURE));
    }
}
