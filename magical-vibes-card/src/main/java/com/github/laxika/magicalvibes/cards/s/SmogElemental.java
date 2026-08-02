package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "GTC", collectorNumber = "79")
public class SmogElemental extends Card {

    public SmogElemental() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(-1, -1, GrantScope.OPPONENT_CREATURES,
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
