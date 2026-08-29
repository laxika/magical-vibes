package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellAndExileAllWithSameNameEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "STX", collectorNumber = "59")
public class TestOfTalents extends Card {

    public TestOfTalents() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                "Target must be an instant or sorcery spell."
        ));
        addEffect(EffectSlot.SPELL, new CounterSpellAndExileAllWithSameNameEffect(true, true));
    }
}
