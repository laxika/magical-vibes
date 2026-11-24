package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetSpellMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryControllerPoisonCountersAtLeastPredicate;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "NPH", collectorNumber = "32")
public class CorruptedResolve extends Card {

    public CorruptedResolve() {
        addSpellEffect(new ConditionalEffect(
                new TargetSpellMatches(new StackEntryControllerPoisonCountersAtLeastPredicate(1)),
                new CounterSpellEffect()));
    }
}
