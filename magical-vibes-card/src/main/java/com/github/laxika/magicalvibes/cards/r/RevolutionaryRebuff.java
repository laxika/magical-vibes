package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryCardTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "KLD", collectorNumber = "61")
public class RevolutionaryRebuff extends Card {

    public RevolutionaryRebuff() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(new StackEntryCardTypeInPredicate(Set.of(CardType.ARTIFACT))),
                "Target must be a nonartifact spell."
        )).addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(2));
    }
}
