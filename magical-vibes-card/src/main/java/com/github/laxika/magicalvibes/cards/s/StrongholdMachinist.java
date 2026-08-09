package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEM", collectorNumber = "46")
public class StrongholdMachinist extends Card {

    public StrongholdMachinist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}{U}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CounterSpellEffect()
                ),
                "{U}{U}, {T}, Discard a card: Counter target noncreature spell.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryNotPredicate(
                                new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL))
                        ),
                        "Target must be a noncreature spell."
                )
        ));
    }
}
