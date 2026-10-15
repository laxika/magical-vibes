package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryIsNthSpellCastThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

@CardRegistration(set = "AVR", collectorNumber = "74")
public class SecondGuess extends Card {

    public SecondGuess() {
        // Counter target spell that's the second spell cast this turn.
        target(new StackEntryPredicateTargetFilter(
                new StackEntryIsNthSpellCastThisTurnPredicate(2),
                "Target must be the second spell cast this turn."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
