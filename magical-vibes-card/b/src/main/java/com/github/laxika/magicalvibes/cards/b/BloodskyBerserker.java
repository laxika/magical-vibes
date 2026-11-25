package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "80")
public class BloodskyBerserker extends Card {

    public BloodskyBerserker() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE, 2),
                new GrantKeywordEffect(Keyword.MENACE, Subjects.source()));
    }
}
