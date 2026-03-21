package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

import java.util.Set;

@CardRegistration(set = "DOM", collectorNumber = "25")
public class KwendePrideOfFemeref extends Card {

    public KwendePrideOfFemeref() {
        // Creatures you control with first strike have double strike.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(0, 0,
                Set.of(Keyword.DOUBLE_STRIKE), GrantScope.ALL_OWN_CREATURES,
                new PermanentHasKeywordPredicate(Keyword.FIRST_STRIKE)));
    }
}
