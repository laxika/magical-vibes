package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsTokenPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.Set;

@CardRegistration(set = "MH2", collectorNumber = "34")
public class ThrabenWatcher extends Card {

    public ThrabenWatcher() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, Set.of(Keyword.VIGILANCE),
                GrantScope.OWN_CREATURES, new PermanentNotPredicate(new PermanentIsTokenPredicate())));
    }
}
