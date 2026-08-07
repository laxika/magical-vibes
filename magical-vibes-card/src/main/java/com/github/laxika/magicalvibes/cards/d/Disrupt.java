package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "WTH", collectorNumber = "37")
public class Disrupt extends Card {

    public Disrupt() {
        // Counter target instant or sorcery spell unless its controller pays {1}.
        // Draw a card.
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                "Target must be an instant or sorcery spell."
        ));
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
