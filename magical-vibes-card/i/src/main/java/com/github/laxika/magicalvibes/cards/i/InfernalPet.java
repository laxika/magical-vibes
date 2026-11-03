package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KHM", collectorNumber = "99")
public class InfernalPet extends Card {

    public InfernalPet() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2),
                new PutCountersOnSourceEffect(1, 1, 1),
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF));
    }
}
