package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryColorInPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import java.util.Set;

@CardRegistration(set = "GPT", collectorNumber = "25")
public class Frazzle extends Card {

    public Frazzle() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(new StackEntryColorInPredicate(Set.of(CardColor.BLUE))),
                "Target must be a nonblue spell."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
