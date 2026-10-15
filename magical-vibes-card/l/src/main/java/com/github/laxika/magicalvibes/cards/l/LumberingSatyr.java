package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "MMQ", collectorNumber = "257")
public class LumberingSatyr extends Card {

    public LumberingSatyr() {
        addEffect(EffectSlot.STATIC,
                new GrantKeywordEffect(Keyword.FORESTWALK, GrantScope.ALL_CREATURES_INCLUDING_SELF));
    }
}
