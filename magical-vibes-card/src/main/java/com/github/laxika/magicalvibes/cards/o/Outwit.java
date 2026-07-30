package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTargetsAnyPlayerPredicate;

@CardRegistration(set = "AVR", collectorNumber = "70")
public class Outwit extends Card {

    public Outwit() {
        // Counter target spell that targets a player.
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTargetsAnyPlayerPredicate(),
                "Target must be a spell that targets a player."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
