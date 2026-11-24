package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.NoteAmountEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "SOK", collectorNumber = "51")
@CardRegistration(set = "DDJ", collectorNumber = "28")
public class OverwhelmingIntellect extends Card {

    public OverwhelmingIntellect() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)),
                "Target must be a creature spell."
        ))
                // Note the target's mana value before countering it, then draw that many cards.
                .addSpellEffect(new NoteAmountEffect(new TargetSpellManaValue()))
                .addSpellEffect(new CounterSpellEffect())
                .addSpellEffect(new DrawCardEffect(AmountThisWay.noted()));
    }
}
