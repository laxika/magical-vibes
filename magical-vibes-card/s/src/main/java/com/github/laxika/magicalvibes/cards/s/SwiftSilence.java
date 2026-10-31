package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.CounterMatchingSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryTruePredicate;

@CardRegistration(set = "DIS", collectorNumber = "132")
public class SwiftSilence extends Card {

    public SwiftSilence() {
        addSpellEffect(new CounterMatchingSpellsEffect(new StackEntryTruePredicate()));
        addSpellEffect(new DrawCardEffect(new EventValue()));
    }
}
