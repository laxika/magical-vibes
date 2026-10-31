package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "SOI", collectorNumber = "204")
public class EquestrianSkill extends Card {

    public EquestrianSkill() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(3, 3, GrantScope.ENCHANTED_CREATURE))
                .addStaticEffect(new EnchantedPermanentConditionalEffect(
                        new PermanentHasSubtypePredicate(CardSubtype.HUMAN),
                        new GrantKeywordEffect(Keyword.TRAMPLE, GrantScope.ENCHANTED_CREATURE),
                        null
                ));
    }
}
