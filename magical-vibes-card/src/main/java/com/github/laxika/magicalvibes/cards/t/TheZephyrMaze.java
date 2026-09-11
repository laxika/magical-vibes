package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OPC2", collectorNumber = "40")
public class TheZephyrMaze extends Card {

    public TheZephyrMaze() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.ALL_CREATURES,
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-2, 0, GrantScope.ALL_CREATURES,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
        target(TargetFilters.creature()).addEffect(EffectSlot.CHAOS_TRIGGERED,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.TARGET));
    }
}
