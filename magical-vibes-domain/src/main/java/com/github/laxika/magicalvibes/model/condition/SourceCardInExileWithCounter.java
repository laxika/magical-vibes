package com.github.laxika.magicalvibes.model.condition;

import com.github.laxika.magicalvibes.model.CounterType;

import java.util.Objects;

/**
 * The source card is in exile with a {@code kind} counter on it: "if this card is exiled with a scream counter on it"
 * (All Hallow's Eve), "with an egg counter on it" (Darigaaz Reincarnated), "in exile with a fetch counter on it" (Hex,
 * Kellan's Companion); read from the one store for counters on exiled cards ({@code GameData.exiledCardCounters}).
 *
 * @param kind the kind of counter
 */
public record SourceCardInExileWithCounter(CounterType kind) implements Condition {

    public SourceCardInExileWithCounter {
        Objects.requireNonNull(kind, "kind");
    }

    @Override
    public String conditionName() {
        return "source card is in exile with a " + kind.name().toLowerCase(java.util.Locale.ROOT) + " counter";
    }

    @Override
    public String conditionNotMetReason() {
        return "source card is not in exile with a " + kind.name().toLowerCase(java.util.Locale.ROOT) + " counter";
    }
}
