package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EOE", collectorNumber = "61")
public class IllvoiOperative extends Card {

    public IllvoiOperative() {
        addTrigger(Triggers.youCastSpell().nthThisTurn(2), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
