package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;

@CardRegistration(set = "INV", collectorNumber = "67")
public class Prohibit extends Card {

    public Prohibit() {
        addEffect(EffectSlot.STATIC, new KickerEffect("{2}"));
        target(new StackEntryPredicateTargetFilter(
                new StackEntryMaxManaValuePredicate(2),
                "Target spell must have mana value 2 or less, or 4 or less if kicked.",
                new StackEntryMaxManaValuePredicate(4)
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
