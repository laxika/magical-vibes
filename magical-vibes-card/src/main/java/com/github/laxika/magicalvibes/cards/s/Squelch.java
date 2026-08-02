package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "CHK", collectorNumber = "92")
public class Squelch extends Card {

    public Squelch() {
        // "Counter target activated ability. Draw a card."
        // Mana abilities never use the stack, so they are excluded automatically.
        // Any player's activated ability is a legal target.
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.ACTIVATED_ABILITY)),
                "Target must be an activated ability."));

        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
