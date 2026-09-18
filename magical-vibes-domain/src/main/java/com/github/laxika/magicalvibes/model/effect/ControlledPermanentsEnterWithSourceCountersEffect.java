package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.EnumMap;
import java.util.Map;

/**
 * Static entry replacement that copies the kinds of counters on the source to each matching
 * permanent as it enters, putting one counter of each copied kind on the entering permanent.
 */
public record ControlledPermanentsEnterWithSourceCountersEffect(
        PermanentPredicate enteringPermanentPredicate
) implements ControlledPermanentEntryReplacementEffect {

    @Override
    public Map<CounterType, Integer> additionalCounters(GameData gameData, Permanent source,
                                                          Permanent enteringPermanent) {
        EnumMap<CounterType, Integer> counters = new EnumMap<>(CounterType.class);
        source.getCounters().forEach((counterType, count) -> {
            if (count > 0) {
                counters.put(counterType, 1);
            }
        });
        return counters;
    }
}
