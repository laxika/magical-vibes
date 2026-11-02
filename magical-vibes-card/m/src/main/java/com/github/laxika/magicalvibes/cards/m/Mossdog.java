package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NEM", collectorNumber = "106")
public class Mossdog extends Card {

    public Mossdog() {
        // Whenever this creature becomes the target of a spell or ability an opponent controls,
        // put a +1/+1 counter on this creature.
        addTrigger(Triggers.becomesTargetOfOpponentSpellOrAbility(),
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
