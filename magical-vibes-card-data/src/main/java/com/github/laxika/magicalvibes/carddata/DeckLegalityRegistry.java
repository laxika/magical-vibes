package com.github.laxika.magicalvibes.carddata;

import com.github.laxika.magicalvibes.cards.CardSet;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DeckFormat;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.time.Instant;
import java.util.Map;
import java.util.Locale;
import java.util.concurrent.*;

@Service
public class DeckLegalityRegistry {
    private final OracleLoader loader;
    private final boolean eager;
    private final Map<String, LegalitySnapshot> snapshots = new ConcurrentHashMap<>();
    private final ScheduledExecutorService refresh = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread thread = new Thread(r, "deck-legality-refresh");
        thread.setDaemon(true);
        return thread;
    });

    public DeckLegalityRegistry(OracleLoader loader,
            @Value("${oracle.data-load-mode:EAGER}") String loadMode) {
        this.loader = loader;
        eager = "EAGER".equalsIgnoreCase(loadMode);
    }

    @PostConstruct void start() {
        if (eager) refresh.scheduleWithFixedDelay(() -> {
            for (CardSet set : CardSet.values()) {
                if (Thread.currentThread().isInterrupted()) return;
                refreshSet(set.getCode());
            }
        }, 0, 24, TimeUnit.HOURS);
    }

    @PreDestroy void stop() { refresh.shutdownNow(); }

    private synchronized LegalitySnapshot refreshSet(String set) {
        LegalitySnapshot snapshot = loader.loadLegalities(set);
        if (snapshot.updatedAt() != null || !snapshots.containsKey(set)) snapshots.put(set, snapshot);
        return snapshots.getOrDefault(set, LegalitySnapshot.empty());
    }

    public LegalitySnapshot snapshot(Card card) {
        if (card.getSetCode() == null) return LegalitySnapshot.empty();
        String set = card.getSetCode().toUpperCase(Locale.ROOT);
        LegalitySnapshot snapshot = snapshots.get(set);
        return snapshot == null ? refreshSet(set) : snapshot;
    }

    public String status(Card card, DeckFormat format) {
        return snapshot(card).cards().getOrDefault(card.getCollectorNumber(), Map.of())
                .getOrDefault(format.name().toLowerCase(Locale.ROOT), "unknown");
    }
}
