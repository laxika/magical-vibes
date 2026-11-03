package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "80")
public class BloodskyBerserker extends Card {

    public BloodskyBerserker() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                new PutCountersOnSourceEffect(1, 1, 2),
                new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF));
    }
}
