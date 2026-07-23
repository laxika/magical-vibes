package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsSourcePredicate;

import java.util.List;

@CardRegistration(set = "ICE", collectorNumber = "84")
public class Mistfolk extends Card {

    public Mistfolk() {
        // {U}: Counter target spell that targets this creature.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{U}",
                List.of(new CounterSpellEffect()),
                "{U}: Counter target spell that targets this creature.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryTargetsSourcePredicate(),
                        "Target must be a spell that targets this creature."
                )
        ));
    }
}
