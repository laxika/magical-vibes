package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

@CardRegistration(set = "ECL", collectorNumber = "71")
@CardRegistration(set = "DIS", collectorNumber = "33")
public class SpellSnare extends Card {

    public SpellSnare() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryManaValuePredicate(2),
                "Target spell must have mana value 2."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
