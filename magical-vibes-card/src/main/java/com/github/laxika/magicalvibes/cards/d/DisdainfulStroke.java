package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryNotPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

@CardRegistration(set = "KTK", collectorNumber = "37")
@CardRegistration(set = "GRN", collectorNumber = "37")
public class DisdainfulStroke extends Card {

    public DisdainfulStroke() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryNotPredicate(new StackEntryMaxManaValuePredicate(3)),
                "Target spell must have mana value 4 or greater."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
