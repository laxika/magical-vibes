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
 * departure batches performed by a caller that can't be stopped and run again. Such a removal can no longer ask a
 * question the removal needs answered before the event (CR 616.1), and there is no fallback any more: reaching one is
 * an engine error. It is logged naming its caller, recorded for a {@link #watch watched} game, and thrown as an
 * {@link IllegalStateException}; the caller has to ask first through {@link DepartureService#depart}.
 */
@Slf4j
public final class LegacyDepartureFallback {

    /** Which question the removal would have had to ask before the event. */
    public enum Kind {
        /** Which of its competing death or graveyard replacements applies. */
        COMPETING_DEATH_REPLACEMENTS,
        /** Whether its lone "may" library replacement (Pulmonic Sliver) applies. */
        MAY_LIBRARY_REPLACEMENT,
        /** Which of its competing destruction replacements saves it. */
        COMPETING_DESTRUCTION_REPLACEMENTS
    }

    /**
     * One removal that reached a question it can't ask.
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

    /** Reports that a removal of {@code permanent} reached a {@code kind} question it can't ask, and throws. */
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
        log.error("Game {} - legacy departure fallback {} for {}, reached from {}; move this caller onto"
                + " DepartureService.depart", gameData.id, kind, permanentName, caller);
        List<Hit> hits = WATCHED.get(gameData.id);
        if (hits != null) {
            hits.add(new Hit(kind, permanentName, caller));
        }
        throw new IllegalStateException("Legacy departure fallback " + kind + " for " + permanentName
                + ", reached from " + caller);
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
