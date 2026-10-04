package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "ROE", collectorNumber = "69")
@CardRegistration(set = "SCD", collectorNumber = "54")
public class GravitationalShift extends Card {

    public GravitationalShift() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.ALL_CREATURES,
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-2, 0, GrantScope.ALL_CREATURES,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
    }
}
