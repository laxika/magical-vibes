package com.github.laxika.magicalvibes.carddata;

import java.time.Instant;
import java.util.Map;

public record LegalitySnapshot(Map<String, Map<String, String>> cards, Instant updatedAt) {
    public LegalitySnapshot {
        cards = cards.entrySet().stream().collect(java.util.stream.Collectors.toUnmodifiableMap(
                Map.Entry::getKey, entry -> Map.copyOf(entry.getValue())));
    }
    public static LegalitySnapshot empty() { return new LegalitySnapshot(Map.of(), null); }
}
