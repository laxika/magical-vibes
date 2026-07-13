package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PayLifeCost;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.List;
import java.util.Set;

@CardRegistration(set = "6ED", collectorNumber = "157")
public class StromgaldCabal extends Card {

    public StromgaldCabal() {
        // {T}, Pay 1 life: Counter target white spell.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new PayLifeCost(1), new CounterSpellEffect()),
                "{T}, Pay 1 life: Counter target white spell.",
                new StackEntryPredicateTargetFilter(
                        new StackEntryColorInPredicate(Set.of(CardColor.WHITE)),
                        "Target spell must be white.")
        ));
    }
}
