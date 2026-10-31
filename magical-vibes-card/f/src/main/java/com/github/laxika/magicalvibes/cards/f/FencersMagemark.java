package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantedPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "GPT", collectorNumber = "65")
public class FencersMagemark extends Card {

    public FencersMagemark() {
        target(TargetFilters.creature())
                .addStaticEffect(new StaticBoostEffect(1, 1, GrantScope.OWN_CREATURES,
                                new PermanentIsEnchantedPredicate()))
                .addStaticEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, GrantScope.OWN_CREATURES,
                                new PermanentIsEnchantedPredicate()));
    }
}
