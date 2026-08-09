package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "NEM", collectorNumber = "45")
public class StrongholdBiologist extends Card {

    public StrongholdBiologist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}{U}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CounterSpellEffect()
                ),
                "{U}{U}, {T}, Discard a card: Counter target creature spell.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)),
                        "Target must be a creature spell."
                )
        ));
    }
}
