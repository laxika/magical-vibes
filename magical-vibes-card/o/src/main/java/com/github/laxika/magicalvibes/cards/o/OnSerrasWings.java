package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GrantSupertypeToEnchantedPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "28")
public class OnSerrasWings extends Card {

    public OnSerrasWings() {
        target(TargetFilters.creature()).addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantKeywordEffect(Set.of(Keyword.FLYING, Keyword.VIGILANCE, Keyword.LIFELINK), GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new GrantSupertypeToEnchantedPermanentEffect(CardSupertype.LEGENDARY));
    }
}
