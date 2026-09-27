package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.MoveCounterFromTargetCreatureToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnTargetForEachLeavingSourceCountersEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.condition.SourceCounterThreshold;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.Arrays;
import java.util.List;

@CardRegistration(set = "NCC", collectorNumber = "19")
@CardRegistration(set = "NCC", collectorNumber = "120")
public class ResourcefulDefense extends Card {

    public ResourcefulDefense() {
        PutCountersOnTargetForEachLeavingSourceCountersEffect transferCounters =
                new PutCountersOnTargetForEachLeavingSourceCountersEffect(new PermanentTruePredicate());
        var targetPermanentYouControl = target(TargetFilters.permanentYouControl());
        targetPermanentYouControl.addEffect(EffectSlot.ON_SELF_LEAVES_BATTLEFIELD,
                new ConditionalEffect(
                        new SourceCounterThreshold(1, CounterType.ANY), transferCounters));
        targetPermanentYouControl.addEffect(EffectSlot.ON_ALLY_PERMANENT_LEAVES_BATTLEFIELD,
                new TriggeringPermanentConditionalEffect(
                        new PermanentHasCountersPredicate(CounterType.ANY), transferCounters));

        addActivatedAbility(new ActivatedAbility(
                false,
                "{4}{W}",
                moveAnyCountersEffects(),
                "{4}{W}: Move any number of counters from target permanent you control onto a second target permanent you control.",
                List.of(TargetFilters.permanentYouControl(), TargetFilters.permanentYouControl()),
                2,
                2
        ));
    }

    private static List<CardEffect> moveAnyCountersEffects() {
        return Arrays.stream(CounterType.values())
                .filter(counterType -> counterType != CounterType.ANY && counterType != CounterType.SILVER)
                .map(MoveCounterFromTargetCreatureToTargetCreatureEffect::new)
                .map(effect -> (CardEffect) effect)
                .toList();
    }
}
