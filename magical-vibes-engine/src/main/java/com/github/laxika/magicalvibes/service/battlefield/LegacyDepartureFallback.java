package com.github.laxika.magicalvibes.service.battlefield;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The guard on the legacy removal wrappers ({@code removePermanentToGraveyard}, {@code sacrificePermanentToGraveyard},
 * {@code destroyPermanentToGraveyard}, {@code tryDestroyPermanent}, {@code removePermanentToPlayerGraveyard}) and on
 * departure batches performed by a caller that can't be stopped and run again. When such a removal needs an answer
 * it can no longer ask before the event (CR 616.1), it falls back to the older behaviour: the creature leaves as a
 * plain exile and its controller picks the rider afterwards, or it stays on the battlefield until a question asked
 * after the fact is answered. Every fallback is reported here with a warning naming its caller, so those callers can
 * be moved onto {@link DepartureService#depart}.
 *
 * <p>Tests can {@link #watch} a game and fail when it reaches a fallback.
 */
@Slf4j
public final class LegacyDepartureFallback {

    /** Which older behaviour a removal fell back to. */
    public enum Kind {
        /** Every option exiled it, so it was exiled at once and its controller then picks the rider. */
        RIDER_CHOSEN_AFTER_EXILE,
        /** It stays on the battlefield until its controller picks one of its competing death replacements. */
        DEATH_REPLACEMENT_ASKED_AFTER_EVENT,
        /** It stays on the battlefield until its controller answers its lone "may" library replacement. */
        MAY_LIBRARY_ASKED_AFTER_EVENT,
        /** It stays on the battlefield until its controller picks which destruction replacement saves it. */
        DESTRUCTION_REPLACEMENT_ASKED_AFTER_EVENT
    }

    /**
     * One fallback.
     *
     * @param caller the first stack frame of the project outside the departure services, as
     *               {@code Class.method:line}
     */
    public record Hit(Kind kind, String permanentName, String caller) {}

    private static final Set<String> DEPARTURE_CLASSES = Set.of(
            LegacyDepartureFallback.class.getName(),
            PermanentRemovalService.class.getName(),
            DepartureService.class.getName(),
            GraveyardService.class.getName());

    /** Only the project's own frames name a caller; Spring's proxy and reflection frames sit between them. */
    private static final String PROJECT_PACKAGE = "com.github.laxika.magicalvibes.";

    private static final StackWalker WALKER = StackWalker.getInstance();

    private static final Map<UUID, List<Hit>> WATCHED = new ConcurrentHashMap<>();

    private LegacyDepartureFallback() {
    }

    /** Reports that a removal of {@code permanent} fell back to the older behaviour {@code kind}. */
    public static void reached(GameData gameData, Kind kind, Permanent permanent) {
        String caller = WALKER.walk(frames -> frames
                .filter(frame -> frame.getClassName().startsWith(PROJECT_PACKAGE)
                        && !frame.getClassName().contains("$$")
                        && !DEPARTURE_CLASSES.contains(frame.getClassName()))
                .findFirst()
                .map(frame -> simpleName(frame.getClassName()) + "." + frame.getMethodName()
                        + ":" + frame.getLineNumber())
                .orElse("unknown"));
        String permanentName = permanent.getCard().getName();
        log.warn("Game {} - legacy departure fallback {} for {}, reached from {}; move this caller onto"
                + " DepartureService.depart", gameData.id, kind, permanentName, caller);
        List<Hit> hits = WATCHED.get(gameData.id);
        if (hits != null) {
            hits.add(new Hit(kind, permanentName, caller));
        }
    }

    /** Starts recording the fallbacks the game {@code gameId} reaches. */
    public static void watch(UUID gameId) {
        WATCHED.put(gameId, Collections.synchronizedList(new ArrayList<>()));
    }

    /** Stops recording for {@code gameId} and returns what it recorded (empty if it wasn't watched). */
    public static List<Hit> unwatch(UUID gameId) {
        List<Hit> hits = WATCHED.remove(gameId);
        if (hits == null) {
            return List.of();
        }
        synchronized (hits) {
            return List.copyOf(hits);
        }
    }

    private static String simpleName(String className) {
        return className.substring(className.lastIndexOf('.') + 1);
    }
}
