package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.TargetSpellMatches;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryControllerPoisonCountersAtLeastPredicate;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;

@CardRegistration(set = "ONE", collectorNumber = "44")
public class BringTheEnding extends Card {

    public BringTheEnding() {
        addSpellEffect(new ConditionalEffect(
                new TargetSpellMatches(new StackEntryControllerPoisonCountersAtLeastPredicate(3)),
                new CounterSpellEffect()));
        addSpellEffect(UnlessPaysEffect.counterUnlessPays(2));
    }
}
