package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEM", collectorNumber = "106")
public class Mossdog extends Card {

    public Mossdog() {
        // Whenever this creature becomes the target of a spell or ability an opponent controls,
        // put a +1/+1 counter on this creature.
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                PutCountersEffect.onSelf(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
