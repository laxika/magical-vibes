package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LearnEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetSpellOrPermanentToHandEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

@CardRegistration(set = "STX", collectorNumber = "41")
public class DivideByZero extends Card {

    public DivideByZero() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(new StackEntryMaxManaValuePredicate(0)),
                "Target must have mana value 1 or greater."))
                .addEffect(EffectSlot.SPELL, new ReturnTargetSpellOrPermanentToHandEffect(1));

        addEffect(EffectSlot.SPELL, new LearnEffect());
    }
}
