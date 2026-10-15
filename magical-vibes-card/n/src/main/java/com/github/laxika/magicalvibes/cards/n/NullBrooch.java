package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardHandCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "EXO", collectorNumber = "136")
public class NullBrooch extends Card {

    public NullBrooch() {
        // {2}, {T}, Discard your hand: Counter target noncreature spell.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{2}",
                List.of(new DiscardHandCost(), new CounterSpellEffect()),
                "{2}, {T}, Discard your hand: Counter target noncreature spell.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryNotPredicate(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))),
                        "Target must be a noncreature spell.")));
    }
}
