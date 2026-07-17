package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5ED", collectorNumber = "154")
public class Deathgrip extends Card {

    public Deathgrip() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{B}{B}",
                List.of(new CounterSpellEffect()),
                "{B}{B}: Counter target green spell.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryColorInPredicate(Set.of(CardColor.GREEN)),
                        "Target must be a green spell.")
        ));
    }
}
