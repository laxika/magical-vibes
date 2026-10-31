package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCastFromZonePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

@CardRegistration(set = "JUD", collectorNumber = "44")
public class LaquatussDisdain extends Card {

    public LaquatussDisdain() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryCastFromZonePredicate(Zone.GRAVEYARD),
                "Target must be a spell cast from a graveyard."
        )).addSpellEffect(new CounterSpellEffect())
                .addSpellEffect(new DrawCardEffect());
    }
}
