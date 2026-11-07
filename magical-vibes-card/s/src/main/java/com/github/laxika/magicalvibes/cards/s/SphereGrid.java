package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PermanentReference;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnReferencedPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.Set;

@CardRegistration(set = "FIC", collectorNumber = "70")
@CardRegistration(set = "FIC", collectorNumber = "123")
public class SphereGrid extends Card {

    public SphereGrid() {
        // Whenever a creature you control deals combat damage to a player, put a +1/+1 counter on that creature.
        addTrigger(Triggers.creatureYouControlDealsCombatDamageToPlayer(),
                new PutCounterOnReferencedPermanentEffect(PermanentReference.TRIGGERING,
                        CounterType.PLUS_ONE_PLUS_ONE));

        // Creatures you control with +1/+1 counters on them have reach and trample.
        addStaticEffect(new GrantKeywordEffect(
                Set.of(Keyword.REACH, Keyword.TRAMPLE),
                GrantScope.ALL_OWN_CREATURES,
                new PermanentHasCountersPredicate(CounterType.PLUS_ONE_PLUS_ONE)));
    }
}
