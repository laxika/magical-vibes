package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.OpponentsCantCastNoncreatureSpellsThisTurnEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "REX", collectorNumber = "17")
@CardRegistration(set = "REX", collectorNumber = "42")
public class PermissionDenied extends Card {

    public PermissionDenied() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))
                ),
                "Target must be a noncreature spell."
        ))
                .addSpellEffect(new CounterSpellEffect())
                .addSpellEffect(new OpponentsCantCastNoncreatureSpellsThisTurnEffect());
    }
}
