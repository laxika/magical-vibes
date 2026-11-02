package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ISD", collectorNumber = "164")
public class StromkirkNoble extends Card {

    public StromkirkNoble() {
        // Stromkirk Noble can't be blocked by Humans
        addStaticEffect(new CanBeBlockedOnlyByFilterEffect(
                new PermanentNotPredicate(new PermanentHasSubtypePredicate(CardSubtype.HUMAN)),
                "non-Human creatures"
        ));

        // Whenever Stromkirk Noble deals combat damage to a player, put a +1/+1 counter on it
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new PutCountersOnSourceEffect(1, 1, 1));
    }
}
