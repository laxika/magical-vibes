package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FCA", collectorNumber = "53")
@CardRegistration(set = "BLC", collectorNumber = "89")
@CardRegistration(set = "C16", collectorNumber = "33")
@CardRegistration(set = "CM2", collectorNumber = "12")
public class IshaiOjutaiDragonspeaker extends Card {

    public IshaiOjutaiDragonspeaker() {
        // Whenever an opponent casts a spell, put a +1/+1 counter on Ishai.
        addTrigger(Triggers.opponentCastsSpell().anySpell(), PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
