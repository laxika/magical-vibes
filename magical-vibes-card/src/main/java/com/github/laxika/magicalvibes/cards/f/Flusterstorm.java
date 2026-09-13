package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;

import java.util.Set;

@CardRegistration(set = "IMA", collectorNumber = "55")
@CardRegistration(set = "VMA", collectorNumber = "68")
public class Flusterstorm extends Card {

    public Flusterstorm() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(
                        StackEntryType.INSTANT_SPELL, StackEntryType.SORCERY_SPELL)),
                "Target must be an instant or sorcery spell."
        ));
        addEffect(EffectSlot.SPELL, new CounterUnlessPaysEffect(1));
        addEffect(EffectSlot.ON_SELF_CAST, new StormEffect());
    }
}
