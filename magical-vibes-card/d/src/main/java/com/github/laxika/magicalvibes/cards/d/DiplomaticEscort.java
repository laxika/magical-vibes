package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsPermanentPredicate;

import java.util.List;

@CardRegistration(set = "MMQ", collectorNumber = "74")
public class DiplomaticEscort extends Card {

    public DiplomaticEscort() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{U}",
                List.of(
                        new DiscardCardTypeCost(null, null),
                        new CounterSpellEffect()
                ),
                "{U}, {T}, Discard a card: Counter target spell or ability that targets a creature.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryTargetsPermanentPredicate(new PermanentIsCreaturePredicate()),
                        "Target must be a spell or ability that targets a creature."
                )
        ));
    }
}
