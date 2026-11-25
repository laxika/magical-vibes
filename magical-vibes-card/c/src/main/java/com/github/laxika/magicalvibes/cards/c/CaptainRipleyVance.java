package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "CMM", collectorNumber = "209")
@CardRegistration(set = "MH2", collectorNumber = "119")
public class CaptainRipleyVance extends Card {

    public CaptainRipleyVance() {
        // Whenever you cast your third spell each turn, put a +1/+1 counter on Captain Ripley
        // Vance, then it deals damage equal to its power to any target.
        addTrigger(Triggers.youCastSpell().nthThisTurn(3),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE),
                DealDamageEffect.toAnyTarget(new SourcePower()));
    }
}
