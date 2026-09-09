package com.github.laxika.magicalvibes.model.planar;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import lombok.Getter;

import java.util.EnumMap;
import java.util.Map;
import java.util.UUID;

/** A face-up command-zone object; its identity lasts until it turns face down. */
@Getter
public final class PlanarObject {
    private final UUID id;
    private final Card card;
    private final long timestamp;
    private final Map<CounterType, Integer> counters = new EnumMap<>(CounterType.class);

    public PlanarObject(Card card, long timestamp) {
        this(UUID.randomUUID(), card, timestamp);
    }

    private PlanarObject(UUID id, Card card, long timestamp) {
        this.id = id;
        this.card = card;
        this.timestamp = timestamp;
        card.freeze();
    }

    public PlanarObject copy() {
        PlanarObject copy = new PlanarObject(id, card, timestamp);
        copy.counters.putAll(counters);
        return copy;
    }
}
