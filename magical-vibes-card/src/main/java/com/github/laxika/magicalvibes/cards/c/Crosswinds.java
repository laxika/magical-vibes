package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "USG", collectorNumber = "246")
public class Crosswinds extends Card {

    public Crosswinds() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-2, 0, GrantScope.ALL_CREATURES,
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
