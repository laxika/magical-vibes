package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileAllWithSameNameEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "SOI", collectorNumber = "68")
@CardRegistration(set = "SIR", collectorNumber = "76")
public class InvasiveSurgery extends Card {

    public InvasiveSurgery() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.SORCERY_SPELL)),
                "Target must be a sorcery spell."
        )).addSpellEffect(new CounterSpellAndExileAllWithSameNameEffect(
                true, false, false, new Delirium()));
    }
}
