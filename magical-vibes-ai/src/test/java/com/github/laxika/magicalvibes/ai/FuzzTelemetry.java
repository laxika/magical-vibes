package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.LongAdder;

/**
 * Batch-wide coverage counters for the AI fuzz tests. The engines record what they actually
 * did (spells cast, abilities activated, interaction prompts handled) and what they skipped
 * and why; the test prints one report at the end of the batch so capability gaps in the
 * Random AI — card families it can never exercise — are visible instead of silent.
 *
 * <p>Thread-safe: both AI engines write from their own executor threads while the test
 * thread reads the report.</p>
 */
final class FuzzTelemetry {

    /** Only cards dealt at least this many times qualify for the never-cast report — a card
     * dealt once may simply never have been drawn. */
    private static final int NEVER_CAST_MIN_COPIES = 3;
    private static final int NEVER_CAST_NAME_CAP = 40;
    private static final int SKIP_NAME_CAP = 15;

    private final ConcurrentMap<String, LongAdder> deckCopies = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LongAdder> spellCasts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LongAdder> abilityActivations = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LongAdder> interactionPrompts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, LongAdder> skipCounts = new ConcurrentHashMap<>();
    private final ConcurrentMap<String, Set<String>> skipCards = new ConcurrentHashMap<>();
    private final LongAdder gamesCompleted = new LongAdder();

    /** Records the nonland cards dealt into a deck (lands are played, not cast). */
    void recordDeckCards(List<Card> deck) {
        for (Card card : deck) {
            if (!card.hasType(CardType.LAND)) {
                increment(deckCopies, card.getName());
            }
        }
    }

    void recordGameCompleted() {
        gamesCompleted.increment();
    }

    void recordSpellCast(String cardName) {
        increment(spellCasts, cardName);
    }

    void recordAbilityActivation(String sourceCardName) {
        increment(abilityActivations, sourceCardName);
    }

    void recordInteractionPrompt(String interactionKind) {
        increment(interactionPrompts, interactionKind);
    }

    /**
     * Records a candidate the Random AI passed over, keyed by reason. Occurrence counts are
     * inflated (the same skip re-fires on every priority scan while the card sits in hand or
     * on the battlefield); the distinct-card list per reason is the coverage signal.
     */
    void recordSkip(String reason, String cardName) {
        increment(skipCounts, reason);
        skipCards.computeIfAbsent(reason, k -> ConcurrentHashMap.newKeySet()).add(cardName);
    }

    private void increment(ConcurrentMap<String, LongAdder> counters, String key) {
        counters.computeIfAbsent(key, k -> new LongAdder()).increment();
    }

    void printReport() {
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("%n=== Fuzz telemetry (%d completed games) ===%n", gamesCompleted.sum()));
        sb.append(String.format("Deck pool:           %d distinct nonland cards, %d copies dealt%n",
                deckCopies.size(), total(deckCopies)));
        sb.append(String.format("Spells cast:         %d (%d distinct cards)%n",
                total(spellCasts), spellCasts.size()));
        sb.append(String.format("Abilities activated: %d (%d distinct sources)%n",
                total(abilityActivations), abilityActivations.size()));

        appendNeverCast(sb);
        appendInteractionPrompts(sb);
        appendSkips(sb);

        System.out.print(sb);
    }

    private void appendNeverCast(StringBuilder sb) {
        List<Map.Entry<String, Long>> neverCast = deckCopies.entrySet().stream()
                .filter(e -> !spellCasts.containsKey(e.getKey()))
                .map(e -> Map.entry(e.getKey(), e.getValue().sum()))
                .filter(e -> e.getValue() >= NEVER_CAST_MIN_COPIES)
                .sorted(Map.Entry.<String, Long>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey()))
                .toList();
        sb.append(String.format("Never cast despite %d+ dealt copies: %d cards%n",
                NEVER_CAST_MIN_COPIES, neverCast.size()));
        appendCappedList(sb, neverCast.stream()
                .map(e -> e.getKey() + " x" + e.getValue())
                .toList(), NEVER_CAST_NAME_CAP);
    }

    private void appendInteractionPrompts(StringBuilder sb) {
        sb.append(String.format("Interaction prompts handled by kind:%n"));
        interactionPrompts.entrySet().stream()
                .sorted(byCountDescThenKey())
                .forEach(e -> sb.append(String.format("  %-45s %8d%n", e.getKey(), e.getValue().sum())));
    }

    private void appendSkips(StringBuilder sb) {
        sb.append(String.format(
                "Skips by reason (occurrences re-fire every priority scan; distinct cards are the signal):%n"));
        skipCounts.entrySet().stream()
                .sorted(byCountDescThenKey())
                .forEach(e -> {
                    Set<String> cards = skipCards.getOrDefault(e.getKey(), Set.of());
                    sb.append(String.format("  %-45s %8d occurrences, %d distinct cards%n",
                            e.getKey(), e.getValue().sum(), cards.size()));
                    appendCappedList(sb, cards.stream().sorted().toList(), SKIP_NAME_CAP);
                });
    }

    private static Comparator<Map.Entry<String, LongAdder>> byCountDescThenKey() {
        return Comparator.comparingLong((Map.Entry<String, LongAdder> e) -> e.getValue().sum())
                .reversed()
                .thenComparing(Map.Entry.comparingByKey());
    }

    private static void appendCappedList(StringBuilder sb, List<String> entries, int cap) {
        if (entries.isEmpty()) {
            return;
        }
        List<String> shown = entries.subList(0, Math.min(cap, entries.size()));
        sb.append("    ").append(String.join(", ", shown));
        if (entries.size() > cap) {
            sb.append(", ... +").append(entries.size() - cap).append(" more");
        }
        sb.append(String.format("%n"));
    }

    private static long total(ConcurrentMap<String, LongAdder> counters) {
        return counters.values().stream().mapToLong(LongAdder::sum).sum();
    }
}
