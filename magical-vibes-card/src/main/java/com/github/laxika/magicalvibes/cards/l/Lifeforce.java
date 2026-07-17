package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "5ED", collectorNumber = "310")
public class Lifeforce extends Card {

    public Lifeforce() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}{G}",
                List.of(new CounterSpellEffect()),
                "{G}{G}: Counter target black spell.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryColorInPredicate(Set.of(CardColor.BLACK)),
                        "Target must be a black spell.")
        ));
    }
}
