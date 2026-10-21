package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DeckFormat;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.Locale;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Format legality per printing, read once per set from the stored set data. Like the rest of a
 * stored set it is never refreshed on its own; deleting the set's file and restarting picks up a
 * newer ban list.
 */
@Service
public class DeckLegalityRegistry {
    private final OracleLoader loader;
    private final Map<String, LegalitySnapshot> snapshots = new ConcurrentHashMap<>();

    public DeckLegalityRegistry(OracleLoader loader) {
        this.loader = loader;
    }

    /** A set whose legality could not be loaded is not remembered, so the next lookup retries. */
    public LegalitySnapshot snapshot(Card card) {
        if (card.getSetCode() == null) return LegalitySnapshot.empty();
        String set = card.getSetCode().toUpperCase(Locale.ROOT);
        LegalitySnapshot snapshot = snapshots.get(set);
        if (snapshot != null) return snapshot;
        snapshot = loader.loadLegalities(set);
        if (snapshot.updatedAt() != null) snapshots.put(set, snapshot);
        return snapshot;
    }

    public String status(Card card, DeckFormat format) {
        return snapshot(card).cards().getOrDefault(card.getCollectorNumber(), Map.of())
                .getOrDefault(format.name().toLowerCase(Locale.ROOT), "unknown");
    }
}
