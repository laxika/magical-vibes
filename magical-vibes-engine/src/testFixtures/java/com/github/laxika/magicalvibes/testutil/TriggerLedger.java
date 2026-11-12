package com.github.laxika.magicalvibes.testutil;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * The trigger ledger: with the environment variable {@value #VARIABLE} set to a file path, every triggered ability
 * that reaches the stack or a holding queue in a card test appends one line to that file. Running a set of card test
 * classes before and after an engine change and diffing the sorted files shows every triggered ability whose stack
 * object changed.
 *
 * <p>A line is {@code <test id> #<n> | <source> | [<effect classes>] | <field>=<value> ...}: the test (its class and
 * method), the ability's position among the test's abilities, its source card's name, the classes of its effects in
 * order, and the fields of the entry that record its trigger event and its choices (targets, X, the event's objects).
 * An id is written by role, not raw: {@code SOURCE} (the ability's source), {@code SELFCARD} (its card), {@code p1} or
 * {@code p2} (a player, in seat order), or the name of the object it names now ({@code perm:}, {@code card:},
 * {@code stack:}), so the same game played twice writes the same lines. A further object of an ability that triggered
 * an additional time is marked {@code (additional)}.
 *
 * <p>Several test JVMs append to one file, so lines of different classes interleave: sort the file before diffing.
 */
public final class TriggerLedger {

    public static final String VARIABLE = "MV_TRIGGER_LEDGER";

    private static final String FILE = System.getenv(VARIABLE);

    private static final ThreadLocal<String> TEST_ID = new ThreadLocal<>();
    private static final ThreadLocal<int[]> SEQUENCE = ThreadLocal.withInitial(() -> new int[1]);

    private static final Pattern RECORD_FIELD_NAME = Pattern.compile("^(triggering|event|target)[A-Z].*|.*AtTrigger$");

    private static final Set<String> NAMED_FIELDS = Set.of(
            "targetId", "attackedTargetId", "attackingPermanentSnapshots", "defendingPlayerId",
            "combatOpponentPowerAtTrigger", "combatOpponentToughnessAtTrigger", "grantingPermanentId",
            "dyingPermanentManaValue", "linkedExiledCardId", "attachedPermanentSnapshot", "sourceGraveyardEntryVersion",
            "sourcePermanentSnapshot", "onceEachTurnActionKey", "xValue", "activePlayerId", "alternateCost",
            "nonTargeting", "controllerId", "sourcePermanentId", "damageAssignments", "removedPermanentControllers",
            "copy");

    /** The fields of {@link StackEntry} a line writes, by name. */
    private static final List<Field> FIELDS = Arrays.stream(StackEntry.class.getDeclaredFields())
            .filter(field -> !Modifier.isStatic(field.getModifiers()) && !field.isSynthetic())
            .filter(field -> RECORD_FIELD_NAME.matcher(field.getName()).matches()
                    || NAMED_FIELDS.contains(field.getName()))
            .sorted(Comparator.comparing(Field::getName))
            .peek(field -> field.setAccessible(true))
            .toList();

    private TriggerLedger() {
    }

    /** Whether the ledger is being written. */
    public static boolean enabled() {
        return FILE != null;
    }

    /** Starts the lines of the test {@code testId} on this thread. */
    public static void beginTest(String testId) {
        TEST_ID.set(testId);
        SEQUENCE.get()[0] = 0;
    }

    /** Appends the line of {@code entry}, a triggered ability that reached the stack or a holding queue. */
    static void record(GameData gameData, StackEntry entry, boolean additional) {
        if (FILE == null) {
            return;
        }
        String testId = TEST_ID.get();
        StringBuilder line = new StringBuilder(testId == null ? "?" : testId)
                .append(" #").append(++SEQUENCE.get()[0])
                .append(" | ").append(entry.getCard() == null ? "null" : entry.getCard().getName())
                .append(additional ? " (additional)" : "")
                .append(" | ").append(entry.getEffectsToResolve().stream()
                        .map(effect -> effect.getClass().getSimpleName()).toList())
                .append(" |");
        for (Field field : FIELDS) {
            Object value;
            try {
                value = field.get(entry);
            } catch (IllegalAccessException e) {
                throw new IllegalStateException(e);
            }
            String written = write(field.getName(), value, entry, gameData);
            if (!written.equals("null") && !written.equals("0") && !written.equals("false")
                    && !written.equals("[]") && !written.equals("M0")) {
                line.append(' ').append(field.getName()).append('=').append(written);
            }
        }
        try {
            Files.writeString(Path.of(FILE), line.append('\n').toString(),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private static String write(String fieldName, Object value, StackEntry entry, GameData gameData) {
        if (fieldName.endsWith("EntryVersion")) {
            return value == null || (value instanceof Number number && number.longValue() == 0) ? "0" : "set";
        }
        return write(value, entry, gameData);
    }

    private static String write(Object value, StackEntry entry, GameData gameData) {
        if (value == null) {
            return "null";
        }
        if (value instanceof Permanent permanent) {
            boolean own = entry.getCard() != null && (permanent.getCard().getId().equals(entry.getCard().getId())
                    || permanent.getOriginalCard() != null
                    && permanent.getOriginalCard().getId().equals(entry.getCard().getId()));
            return "P(" + (own ? "SOURCE" : permanent.getCard().getName()) + ")";
        }
        if (value instanceof StackEntry other) {
            return "S(" + (other.getCard() == null ? "?" : other.getCard().getName()) + ")";
        }
        if (value instanceof Card card) {
            return entry.getCard() != null && card.getId().equals(entry.getCard().getId())
                    ? "C(SELF)" : "C(" + card.getName() + ")";
        }
        if (value instanceof UUID uuid) {
            return id(uuid, entry, gameData);
        }
        if (value instanceof Map<?, ?> map) {
            StringJoiner joined = new StringJoiner(",", "{", "}");
            map.entrySet().stream()
                    .map(e -> write(e.getKey(), entry, gameData) + ":" + write(e.getValue(), entry, gameData))
                    .sorted().forEach(joined::add);
            return map.isEmpty() ? "M0" : joined.toString();
        }
        if (value instanceof Collection<?> collection) {
            return collection.stream().map(element -> write(element, entry, gameData)).sorted().toList().toString();
        }
        if (value instanceof String) {
            return "str";
        }
        if (value instanceof Number || value instanceof Boolean || value instanceof Enum<?>) {
            return value.toString();
        }
        return value.getClass().getSimpleName();
    }

    private static String id(UUID uuid, StackEntry entry, GameData gameData) {
        if (uuid.equals(entry.getSourcePermanentId())) {
            return "SOURCE";
        }
        if (entry.getCard() != null && uuid.equals(entry.getCard().getId())) {
            return "SELFCARD";
        }
        int seat = gameData.orderedPlayerIds.indexOf(uuid);
        if (seat >= 0) {
            return "p" + (seat + 1);
        }
        for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
            for (Permanent permanent : battlefield) {
                if (permanent.getId().equals(uuid)) {
                    return "perm:" + permanent.getCard().getName();
                }
            }
        }
        for (Map<UUID, ? extends List<Card>> zone : List.of(gameData.playerGraveyards, gameData.playerHands)) {
            for (List<Card> cards : zone.values()) {
                for (Card card : cards) {
                    if (card.getId().equals(uuid)) {
                        return "card:" + card.getName();
                    }
                }
            }
        }
        for (var exiled : gameData.exiledCards) {
            if (exiled.card().getId().equals(uuid)) {
                return "card:" + exiled.card().getName();
            }
        }
        synchronized (gameData.stack) {
            for (StackEntry other : gameData.stack) {
                if (uuid.equals(other.getTargetableId()) && other.getCard() != null) {
                    return "stack:" + other.getCard().getName();
                }
            }
        }
        return "id";
    }
}
