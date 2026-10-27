package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * An immutable view of a permanent's (or a player's) "until end of turn" / "this turn" state, or of
 * the part of it that was created while one particular player was the active player. Runed Terror's
 * sequential phases give every player a cleanup step each turn cycle, and such an effect ends only
 * at the cleanup step of the player who was active when it began (Runed Terror ruling, CR 514.2), so
 * the cleanup step keeps the shares of the other players.
 *
 * <p>Only fields holding a non-default value (not {@code false}, {@code 0}, {@code null} or empty) are
 * present. How each field is split and recombined depends on its {@link EndOfTurnStateField.Kind}.
 *
 * @param values the non-default field values; collections are immutable copies
 */
public record EndOfTurnStateShare(Map<EndOfTurnStateField, Object> values) {

    public static final EndOfTurnStateShare EMPTY = new EndOfTurnStateShare(Map.of());

    public EndOfTurnStateShare {
        Map<EndOfTurnStateField, Object> normalized = new EnumMap<>(EndOfTurnStateField.class);
        values.forEach((field, value) -> {
            Object copy = immutableCopy(field, value);
            if (copy != null) {
                normalized.put(field, copy);
            }
        });
        values = Collections.unmodifiableMap(normalized);
    }

    /** Builds a share from raw (possibly mutable or default) values, dropping the default ones. */
    public static EndOfTurnStateShare of(Map<EndOfTurnStateField, Object> rawValues) {
        return new EndOfTurnStateShare(rawValues);
    }

    /** Combines the shares in order, later shares taking precedence for {@code VALUE} fields. */
    public static EndOfTurnStateShare sum(Collection<EndOfTurnStateShare> shares) {
        EndOfTurnStateShare total = EMPTY;
        for (EndOfTurnStateShare share : shares) {
            total = total.plus(share);
        }
        return total;
    }

    public boolean isEmpty() {
        return values.isEmpty();
    }

    public Object get(EndOfTurnStateField field) {
        return values.get(field);
    }

    /**
     * Treating this share as the current state, returns what changed since {@code attributed}, the
     * combination of every share already credited to some player.
     */
    public EndOfTurnStateShare since(EndOfTurnStateShare attributed) {
        Map<EndOfTurnStateField, Object> delta = new EnumMap<>(EndOfTurnStateField.class);
        for (EndOfTurnStateField field : EndOfTurnStateField.values()) {
            Object current = values.get(field);
            Object old = attributed.values.get(field);
            if (current == null && old == null) {
                continue;
            }
            switch (field.kind()) {
                case FLAG -> {
                    if (current != null && old == null) {
                        delta.put(field, Boolean.TRUE);
                    }
                }
                case ADDITIVE, SHIELD -> delta.put(field, SaturatingMath.subtract(intValue(current), intValue(old)));
                case VALUE -> {
                    if (current != null && !Objects.equals(current, old)) {
                        delta.put(field, current);
                    }
                }
                case SET -> {
                    Set<Object> added = new HashSet<>(setValue(current));
                    added.removeAll(setValue(old));
                    delta.put(field, added);
                }
                case LIST -> delta.put(field, listMinus(listValue(current), listValue(old), false));
                case IDENTITY_LIST -> delta.put(field, listMinus(listValue(current), listValue(old), true));
                case COUNTS -> delta.put(field, combineCounts(countsValue(current), countsValue(old), -1));
                case SLOT_LISTS -> {
                    Map<EffectSlot, List<CardEffect>> added = new EnumMap<>(EffectSlot.class);
                    Map<EffectSlot, List<CardEffect>> oldSlots = slotValue(old);
                    slotValue(current).forEach((slot, effects) ->
                            added.put(slot, listMinus(effects, oldSlots.getOrDefault(slot, List.of()), false)));
                    delta.put(field, added);
                }
            }
        }
        return new EndOfTurnStateShare(delta);
    }

    /** Returns the combination of this share and the later share {@code other}. */
    public EndOfTurnStateShare plus(EndOfTurnStateShare other) {
        Map<EndOfTurnStateField, Object> combined = new EnumMap<>(EndOfTurnStateField.class);
        for (EndOfTurnStateField field : EndOfTurnStateField.values()) {
            Object mine = values.get(field);
            Object theirs = other.values.get(field);
            if (mine == null || theirs == null) {
                Object only = mine != null ? mine : theirs;
                if (only != null) {
                    combined.put(field, only);
                }
                continue;
            }
            switch (field.kind()) {
                case FLAG -> combined.put(field, Boolean.TRUE);
                case ADDITIVE, SHIELD -> combined.put(field, SaturatingMath.add(intValue(mine), intValue(theirs)));
                case VALUE -> combined.put(field, theirs);
                case SET -> {
                    Set<Object> union = new HashSet<>(setValue(mine));
                    union.addAll(setValue(theirs));
                    combined.put(field, union);
                }
                case LIST, IDENTITY_LIST -> {
                    List<Object> concat = new ArrayList<>(listValue(mine));
                    concat.addAll(listValue(theirs));
                    combined.put(field, concat);
                }
                case COUNTS -> combined.put(field, combineCounts(countsValue(mine), countsValue(theirs), 1));
                case SLOT_LISTS -> {
                    Map<EffectSlot, List<CardEffect>> merged = new EnumMap<>(EffectSlot.class);
                    slotValue(mine).forEach((slot, effects) -> merged.put(slot, new ArrayList<>(effects)));
                    slotValue(theirs).forEach((slot, effects) ->
                            merged.computeIfAbsent(slot, s -> new ArrayList<>()).addAll(effects));
                    combined.put(field, merged);
                }
            }
        }
        return new EndOfTurnStateShare(combined);
    }

    /**
     * Restricts this share to what is still present in {@code current}: a flag or set element that was
     * since removed and a shield that was since used up are not brought back. {@code ADDITIVE} and
     * {@code VALUE} fields are kept as they are.
     */
    public EndOfTurnStateShare cappedBy(EndOfTurnStateShare current) {
        Map<EndOfTurnStateField, Object> capped = new EnumMap<>(EndOfTurnStateField.class);
        values.forEach((field, mine) -> {
            Object now = current.values.get(field);
            switch (field.kind()) {
                case ADDITIVE, VALUE -> capped.put(field, mine);
                case FLAG -> {
                    if (now != null) {
                        capped.put(field, mine);
                    }
                }
                case SHIELD -> capped.put(field, Math.max(0, Math.min(intValue(mine), intValue(now))));
                case SET -> {
                    Set<Object> kept = new HashSet<>(setValue(mine));
                    kept.retainAll(setValue(now));
                    capped.put(field, kept);
                }
                case LIST -> capped.put(field, listIntersection(listValue(mine), listValue(now), false));
                case IDENTITY_LIST -> capped.put(field, listIntersection(listValue(mine), listValue(now), true));
                case COUNTS -> {
                    Map<Object, Integer> nowCounts = countsValue(now);
                    Map<Object, Integer> kept = new HashMap<>();
                    countsValue(mine).forEach((key, count) ->
                            kept.put(key, Math.max(0, Math.min(count, nowCounts.getOrDefault(key, 0)))));
                    capped.put(field, kept);
                }
                case SLOT_LISTS -> {
                    Map<EffectSlot, List<CardEffect>> kept = new EnumMap<>(EffectSlot.class);
                    Map<EffectSlot, List<CardEffect>> nowSlots = slotValue(now);
                    slotValue(mine).forEach((slot, effects) ->
                            kept.put(slot, listIntersection(effects, nowSlots.getOrDefault(slot, List.of()), false)));
                    capped.put(field, kept);
                }
            }
        });
        return new EndOfTurnStateShare(capped);
    }

    /**
     * Returns this share with one occurrence of {@code previous} in the {@link EndOfTurnStateField.Kind#LIST} field
     * {@code field} replaced by {@code replacement}, or this share itself when the field holds no such element. Used
     * when a shield's record is replaced by a reduced copy, so the copy stays credited to the same player.
     */
    public EndOfTurnStateShare withElementReplaced(EndOfTurnStateField field, Object previous, Object replacement) {
        if (field.kind() != EndOfTurnStateField.Kind.LIST) {
            throw new IllegalArgumentException("Not a LIST field: " + field);
        }
        List<Object> elements = new ArrayList<>(listValue(values.get(field)));
        int index = elements.indexOf(previous);
        if (index < 0) {
            return this;
        }
        elements.set(index, replacement);
        Map<EndOfTurnStateField, Object> replaced = new EnumMap<>(EndOfTurnStateField.class);
        replaced.putAll(values);
        replaced.put(field, elements);
        return new EndOfTurnStateShare(replaced);
    }

    /** Reads an {@code int} field, {@code 0} when absent. */
    public int intValue(EndOfTurnStateField field) {
        return intValue(values.get(field));
    }

    /** Reads a {@code boolean} field, {@code false} when absent. */
    public boolean flag(EndOfTurnStateField field) {
        return values.containsKey(field);
    }

    /** Reads a collection field as a list (empty when absent). */
    @SuppressWarnings("unchecked")
    public <T> List<T> elements(EndOfTurnStateField field) {
        Object value = values.get(field);
        if (value == null) {
            return List.of();
        }
        return value instanceof Set<?> set ? List.copyOf((Set<T>) set) : (List<T>) value;
    }

    /** Reads a {@code COUNTS} field (empty when absent). */
    @SuppressWarnings("unchecked")
    public <K> Map<K, Integer> counts(EndOfTurnStateField field) {
        return (Map<K, Integer>) countsValue(values.get(field));
    }

    /** Reads the {@code SLOT_LISTS} field (empty when absent). */
    public Map<EffectSlot, List<CardEffect>> slotLists(EndOfTurnStateField field) {
        return slotValue(values.get(field));
    }

    private static Object immutableCopy(EndOfTurnStateField field, Object value) {
        if (value == null) {
            return null;
        }
        return switch (field.kind()) {
            case FLAG -> Boolean.TRUE.equals(value) ? Boolean.TRUE : null;
            case ADDITIVE, SHIELD -> intValue(value) == 0 ? null : value;
            case VALUE -> value instanceof Integer i && i == 0 ? null : value;
            case SET -> setValue(value).isEmpty() ? null : Set.copyOf(setValue(value));
            case LIST, IDENTITY_LIST -> listValue(value).isEmpty() ? null : List.copyOf(listValue(value));
            case COUNTS -> {
                Map<Object, Integer> counts = new HashMap<>();
                countsValue(value).forEach((key, count) -> {
                    if (count != null && count != 0) {
                        counts.put(key, count);
                    }
                });
                yield counts.isEmpty() ? null : Map.copyOf(counts);
            }
            case SLOT_LISTS -> {
                Map<EffectSlot, List<CardEffect>> slots = new EnumMap<>(EffectSlot.class);
                slotValue(value).forEach((slot, effects) -> {
                    if (!effects.isEmpty()) {
                        slots.put(slot, List.copyOf(effects));
                    }
                });
                yield slots.isEmpty() ? null : Collections.unmodifiableMap(slots);
            }
        };
    }

    private static int intValue(Object value) {
        return value == null ? 0 : (Integer) value;
    }

    @SuppressWarnings("unchecked")
    private static Set<Object> setValue(Object value) {
        return value == null ? Set.of() : (Set<Object>) value;
    }

    @SuppressWarnings("unchecked")
    private static List<Object> listValue(Object value) {
        return value == null ? List.of() : (List<Object>) value;
    }

    @SuppressWarnings("unchecked")
    private static Map<Object, Integer> countsValue(Object value) {
        return value == null ? Map.of() : (Map<Object, Integer>) value;
    }

    /** Per key, {@code first + sign * second}. */
    private static Map<Object, Integer> combineCounts(Map<Object, Integer> first, Map<Object, Integer> second, int sign) {
        Map<Object, Integer> combined = new HashMap<>(first);
        second.forEach((key, count) -> combined.merge(key, sign * count, SaturatingMath::add));
        return combined;
    }

    @SuppressWarnings("unchecked")
    private static Map<EffectSlot, List<CardEffect>> slotValue(Object value) {
        return value == null ? Map.of() : (Map<EffectSlot, List<CardEffect>>) value;
    }

    /** {@code from} with one occurrence of each element of {@code removed} taken out. */
    private static <T> List<T> listMinus(List<T> from, List<T> removed, boolean identity) {
        List<T> remaining = new ArrayList<>(from);
        for (T element : removed) {
            removeOne(remaining, element, identity);
        }
        return remaining;
    }

    /**
     * The elements of {@code mine} that still occur in {@code current}, counting multiplicity, in their order in
     * {@code current}, so an ordered store (a shield list whose first matching shield applies first) is restored in
     * its live order.
     */
    private static <T> List<T> listIntersection(List<T> mine, List<T> current, boolean identity) {
        List<T> wanted = new ArrayList<>(mine);
        List<T> kept = new ArrayList<>();
        for (T element : current) {
            if (removeOne(wanted, element, identity)) {
                kept.add(element);
            }
        }
        return kept;
    }

    private static <T> boolean removeOne(List<T> list, T element, boolean identity) {
        for (int i = 0; i < list.size(); i++) {
            if (identity ? list.get(i) == element : Objects.equals(list.get(i), element)) {
                list.remove(i);
                return true;
            }
        }
        return false;
    }
}
