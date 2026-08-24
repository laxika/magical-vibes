package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.StackEntryTypeInPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Set;

@CardRegistration(set = "RNA", collectorNumber = "37")
public class EssenceCapture extends Card {

    public EssenceCapture() {
        target(new StackEntryPredicateTargetFilter(
                new StackEntryTypeInPredicate(Set.of(StackEntryType.CREATURE_SPELL)),
                "Target must be a creature spell."
        )).addEffect(EffectSlot.SPELL, new CounterSpellEffect());

        target(TargetFilters.creatureYouControl(), 0, 1)
                .addEffect(EffectSlot.SPELL,
                        new PutCounterOnTargetPermanentEffect(CounterType.PLUS_ONE_PLUS_ONE));
    }
}
