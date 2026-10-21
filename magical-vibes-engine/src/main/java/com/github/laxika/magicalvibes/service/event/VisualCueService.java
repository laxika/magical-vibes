package com.github.laxika.magicalvibes.service.event;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.event.GameEventAudience;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.model.event.VisualCueType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Deque;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.WeakHashMap;

/**
 * The one way engine code emits a {@link GameEventFact.VisualCue}. It owns the audience default
 * and the redaction of hidden sources, so emitting chokepoints only say what happened.
 *
 * <p>Cues are cosmetic: no rules code may read them, and emitting one never changes the game.
 * Emitting is a no-op in a simulation copy (whose batches are never dispatched anyway) and
 * outside a mutation scope, such as test setup that moves cards directly, so a chokepoint never
 * has to know which context it runs in.
 *
 * <p>It also remembers which spell or ability is resolving ({@link #beginResolution} /
 * {@link #endResolution}, called by the effect resolution loop), so a chokepoint that is not
 * handed the resolving entry, such as permanent removal, can still name it as the cue's source.
 * That memory is cosmetic, lives outside {@link GameData} (weakly keyed, like the coordinator's
 * action state) and is never read by rules code.
 *
 * <p>Counters are written from hundreds of engine paths, so {@link VisualCueType#COUNTERS_ADDED}
 * is not emitted by them. Instead the service snapshots every permanent's counters when an outer
 * action starts ({@link #actionStarted}) and emits the increases since the last snapshot, one cue
 * per permanent and counter type, before every other cue, at every resolution boundary and when
 * the action completes ({@link #actionCompleting}). That keeps counter cues in engine order,
 * merges several placements in one window into one cue, and names the resolving source.
 */
@Service
@RequiredArgsConstructor
public class VisualCueService {

    private final GameMutationCoordinator mutationCoordinator;
    private final Map<GameData, Deque<StackEntry>> resolvingEntries =
            Collections.synchronizedMap(new WeakHashMap<>());
    private final Map<GameData, Map<UUID, Map<CounterType, Integer>>> counterSnapshots =
            Collections.synchronizedMap(new WeakHashMap<>());

    /**
     * Called by the mutation coordinator when an outer action starts: takes the counter snapshot
     * later increases are measured against, so changes made outside an action (test setup) are
     * never reported.
     */
    public void actionStarted(GameData gameData) {
        if (gameData.simulation) {
            return;
        }
        counterSnapshots.put(gameData, snapshotCounters(gameData));
    }

    /** Called by the mutation coordinator just before an outer action's batch is completed. */
    public void actionCompleting(GameData gameData) {
        flushCounterCues(gameData);
        counterSnapshots.remove(gameData);
    }

    /** Marks {@code entry} as resolving until the matching {@link #endResolution}; calls nest. */
    public void beginResolution(GameData gameData, StackEntry entry) {
        if (gameData.simulation) {
            return;
        }
        flushCounterCues(gameData);
        resolvingEntries.computeIfAbsent(gameData, ignored -> new ArrayDeque<>()).push(entry);
    }

    /** Ends the innermost {@link #beginResolution}. */
    public void endResolution(GameData gameData) {
        if (gameData.simulation) {
            return;
        }
        Deque<StackEntry> entries = resolvingEntries.get(gameData);
        if (entries == null) {
            return;
        }
        flushCounterCues(gameData);
        entries.poll();
        if (entries.isEmpty()) {
            resolvingEntries.remove(gameData);
        }
    }

    /**
     * The spell or ability that is resolving, else the one parked while a player answers a
     * question it asked, as a cue source; {@link Source#none()} when there is neither (combat,
     * costs, state-based actions between resolutions).
     */
    public Source resolvingSource(GameData gameData) {
        Deque<StackEntry> entries = resolvingEntries.get(gameData);
        StackEntry entry = entries != null ? entries.peek() : null;
        if (entry == null) {
            entry = gameData.pendingEffectResolutionEntry;
        }
        return entry == null ? Source.none() : Source.of(gameData, entry);
    }

    /**
     * A cue source given as a card: the battlefield permanent showing it (so a face-down one stays
     * anonymous), else the {@link #resolvingSource resolving} spell or ability. A {@code null} card
     * also falls back to the resolving source.
     */
    public Source sourceOf(GameData gameData, Card card) {
        if (card != null) {
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                for (Permanent permanent : battlefield) {
                    if (permanent.getCard().getId().equals(card.getId())) {
                        return Source.of(permanent);
                    }
                }
            }
        }
        return resolvingSource(gameData);
    }

    /**
     * Emits a cue to every player. Use this for anything that happened in a public zone.
     */
    public void emit(GameData gameData, VisualCueType type, Source source,
                     UUID targetPermanentId, UUID targetPlayerId, Integer amount) {
        emit(gameData, type, source, targetPermanentId, targetPlayerId, amount,
                GameEventAudience.allPlayers());
    }

    /**
     * Emits a cue to an explicit audience. A cue about a card in a hidden zone must name only
     * the players who may see that card.
     */
    public void emit(GameData gameData, VisualCueType type, Source source,
                     UUID targetPermanentId, UUID targetPlayerId, Integer amount,
                     GameEventAudience audience) {
        emit(gameData, type, source, targetPermanentId, targetPlayerId, null, amount, audience);
    }

    /**
     * Emits a cue whose target may also be a spell on the stack ({@code targetCardId}, the
     * spell's card id), to an explicit audience.
     */
    public void emit(GameData gameData, VisualCueType type, Source source,
                     UUID targetPermanentId, UUID targetPlayerId, UUID targetCardId, Integer amount,
                     GameEventAudience audience) {
        Objects.requireNonNull(gameData, "gameData");
        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(source, "source");
        Objects.requireNonNull(audience, "audience");
        if (gameData.simulation || !mutationCoordinator.isInAction(gameData)) {
            return;
        }
        flushCounterCues(gameData);
        mutationCoordinator.emit(gameData,
                new GameEventFact.VisualCue(type, source.cardId(), source.name(), source.colors(),
                        targetPermanentId, targetPlayerId, targetCardId, amount, null),
                audience);
    }

    /**
     * Emits a {@link VisualCueType#COUNTERS_ADDED} cue for every permanent and counter type whose
     * count rose since the last snapshot, then takes a new one. Battlefields are public, so the cues
     * go to every player.
     */
    private void flushCounterCues(GameData gameData) {
        Map<UUID, Map<CounterType, Integer>> before = counterSnapshots.get(gameData);
        if (before == null || gameData.simulation || !mutationCoordinator.isInAction(gameData)) {
            return;
        }
        Map<UUID, Map<CounterType, Integer>> after = snapshotCounters(gameData);
        counterSnapshots.put(gameData, after);
        Source source = null;
        for (Map.Entry<UUID, Map<CounterType, Integer>> permanent : after.entrySet()) {
            Map<CounterType, Integer> previous = before.getOrDefault(permanent.getKey(), Map.of());
            for (Map.Entry<CounterType, Integer> counters : permanent.getValue().entrySet()) {
                int added = counters.getValue() - previous.getOrDefault(counters.getKey(), 0);
                if (added <= 0) {
                    continue;
                }
                if (source == null) {
                    source = resolvingSource(gameData);
                }
                mutationCoordinator.emit(gameData,
                        new GameEventFact.VisualCue(VisualCueType.COUNTERS_ADDED, source.cardId(),
                                source.name(), source.colors(), permanent.getKey(), null, null, added,
                                counters.getKey()),
                        GameEventAudience.allPlayers());
            }
        }
    }

    /** Every battlefield permanent's counters, in turn order and battlefield order. */
    private static Map<UUID, Map<CounterType, Integer>> snapshotCounters(GameData gameData) {
        Map<UUID, Map<CounterType, Integer>> snapshot = new LinkedHashMap<>();
        for (UUID playerId : gameData.orderedPlayerIds) {
            for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
                Map<CounterType, Integer> counters = new EnumMap<>(CounterType.class);
                for (CounterType type : CounterType.values()) {
                    if (permanent.getTotalCounterCount() == 0) {
                        break;
                    }
                    if (type == CounterType.ANY || type == CounterType.SILVER) {
                        continue;
                    }
                    int count = permanent.getCounterCount(type);
                    if (count > 0) {
                        counters.put(type, count);
                    }
                }
                snapshot.put(permanent.getId(), counters);
            }
        }
        return snapshot;
    }

    /**
     * The source of a cue as the client may see it. Build one with the static factories, which
     * redact what a face-down source hides.
     */
    public record Source(UUID cardId, String name, List<CardColor> colors) {

        private static final Source NONE = new Source(null, null, List.of());

        public Source {
            colors = colors == null ? List.of() : List.copyOf(colors);
        }

        /** No source, such as a state-based death. */
        public static Source none() {
            return NONE;
        }

        /** A face-up card in a public zone. */
        public static Source of(Card card) {
            return new Source(card.getId(), card.getName(), card.getColors());
        }

        /** A permanent; a face-down one keeps its card id but hides its name and colors. */
        public static Source of(Permanent permanent) {
            return permanent.isFaceDown()
                    ? hidden(permanent.getCard())
                    : of(permanent.getCard());
        }

        /** A spell or ability on the stack; a face-down spell hides its name and colors. */
        public static Source of(StackEntry entry) {
            if (entry.getCard() == null) {
                return NONE;
            }
            return entry.isCastFaceDown()
                    ? hidden(entry.getCard())
                    : of(entry.getCard());
        }

        /**
         * A spell or ability on the stack; an ability of a permanent is sourced by that permanent
         * (or its last-known snapshot once it left), so a face-down source stays anonymous.
         */
        public static Source of(GameData gameData, StackEntry entry) {
            UUID sourcePermanentId = entry.getSourcePermanentId();
            if (sourcePermanentId == null) {
                return of(entry);
            }
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                for (Permanent permanent : battlefield) {
                    if (permanent.getId().equals(sourcePermanentId)) {
                        return of(permanent);
                    }
                }
            }
            Permanent snapshot = entry.getSourcePermanentSnapshot();
            return snapshot != null ? of(snapshot) : of(entry);
        }

        private static Source hidden(Card card) {
            return new Source(card.getId(), null, List.of());
        }
    }
}
