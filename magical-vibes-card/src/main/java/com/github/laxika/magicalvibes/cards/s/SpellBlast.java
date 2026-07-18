package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValueEqualsXPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "6ED", collectorNumber = "100")
@CardRegistration(set = "5ED", collectorNumber = "126")
@CardRegistration(set = "4ED", collectorNumber = "103")
public class SpellBlast extends Card {

    public SpellBlast() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryManaValueEqualsXPredicate(),
                "Target spell's mana value must equal X."
        ))
                .addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
