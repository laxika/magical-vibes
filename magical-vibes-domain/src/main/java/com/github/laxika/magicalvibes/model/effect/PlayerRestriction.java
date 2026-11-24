package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardMaxManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardMinManaValuePredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;

import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * What a {@link PlayerRestrictionEffect} stops its players doing: casting spells (every spell, those of some card
 * types, those matching a filter, or noncreature spells), playing lands, playing cards (lands and spells, CR 701.18b)
 * from some zones, or activating abilities that aren't mana abilities. Built through the factories; each reader in
 * the engine answers one of these questions.
 *
 * <p>A spell, land or card-play restriction may name the zones it applies to ({@link #from}, {@link #fromAnywhereBut}):
 * "can't cast spells from anywhere other than their hands" (Avatar's Wrath, Drannith Magistrate), "can't play land
 * cards from graveyards" (Tomik), "can't cast noncreature spells from graveyards or exile" (Soulless Jailer). A
 * restriction with zones names no types and no filter, and a restriction with a filter names no types. A zone-free,
 * type-free, filter-free spell restriction stops every spell ({@link #stopsEverySpell()}); the readers of a zone-free
 * kind ({@code GameData.isRestricted}) don't see a zone-bound one.
 *
 * @param kind        which action is restricted
 * @param spellTypes  for {@link Kind#CAST_SPELLS}, the card types of the spells that can't be cast, in the order the card
 *                    names them ("instant or sorcery"); empty for every spell, and for every other kind
 * @param spellFilter for {@link Kind#CAST_SPELLS}, which spells can't be cast ("spells with the same mana value as that
 *                    spell", fixed as the effect resolves); {@code null} for every spell
 * @param zones       the zones the action is restricted from, or with {@code exceptZones} the zones it is still allowed
 *                    from; {@code null} for every zone
 * @param exceptZones whether {@code zones} names the zones that stay allowed
 */
public record PlayerRestriction(Kind kind, List<CardType> spellTypes, CardPredicate spellFilter, Set<Zone> zones,
                                boolean exceptZones) {

    /** The action a {@link PlayerRestriction} stops. */
    public enum Kind {
        /** "Can't cast spells", or "can't cast [type] spells" when {@link #spellTypes} names types. */
        CAST_SPELLS,
        /**
         * "Can't cast noncreature spells". Not a set of types: an artifact creature spell is a creature spell, so
         * "artifact" would stop a spell this lets through.
         */
        CAST_NONCREATURE_SPELLS,
        /** "Can't play lands". */
        PLAY_LANDS,
        /** "Can't play cards [from zones]": neither play lands nor cast spells from those zones (CR 701.18b). */
        PLAY_CARDS,
        /** "Can't activate abilities that aren't mana abilities" (loyalty abilities included). */
        ACTIVATE_NON_MANA_ABILITIES
    }

    public PlayerRestriction {
        Objects.requireNonNull(kind, "kind");
        spellTypes = List.copyOf(spellTypes);
        zones = zones == null ? null : Set.copyOf(zones);
        if (kind != Kind.CAST_SPELLS && !spellTypes.isEmpty()) {
            throw new IllegalArgumentException("Only a spell restriction names spell types: " + kind);
        }
        if (new HashSet<>(spellTypes).size() != spellTypes.size()) {
            throw new IllegalArgumentException("A spell type is named once: " + spellTypes);
        }
        if (spellTypes.contains(CardType.LAND)) {
            throw new IllegalArgumentException("A land is not a spell; restrict playing lands instead");
        }
        if (spellFilter != null && (kind != Kind.CAST_SPELLS || !spellTypes.isEmpty())) {
            throw new IllegalArgumentException("Only a spell restriction naming no type takes a filter: " + kind);
        }
        if (zones != null && (zones.isEmpty() || kind == Kind.ACTIVATE_NON_MANA_ABILITIES
                || !spellTypes.isEmpty() || spellFilter != null)) {
            throw new IllegalArgumentException("Only a plain spell, land or card-play restriction names zones: " + kind);
        }
        if (kind == Kind.PLAY_CARDS && zones == null) {
            throw new IllegalArgumentException("A card-play restriction names its zones");
        }
        if (exceptZones && zones == null) {
            throw new IllegalArgumentException("No zones to except");
        }
    }

    private PlayerRestriction(Kind kind, List<CardType> spellTypes) {
        this(kind, spellTypes, null, null, false);
    }

    /** "Can't cast spells". */
    public static PlayerRestriction castingSpells() {
        return new PlayerRestriction(Kind.CAST_SPELLS, List.of());
    }

    /** "Can't cast [type] or [type] spells", the types in the order the card names them. */
    public static PlayerRestriction castingSpellsOf(CardType... types) {
        if (types.length == 0) {
            throw new IllegalArgumentException("Name at least one spell type, or use castingSpells()");
        }
        return new PlayerRestriction(Kind.CAST_SPELLS, List.of(types));
    }

    /**
     * "Can't cast spells [matching]": the filter as the effect resolves fixes it ("with the same mana value as that
     * spell", Living Breakthrough).
     */
    public static PlayerRestriction castingSpellsMatching(CardPredicate filter) {
        return new PlayerRestriction(Kind.CAST_SPELLS, List.of(), Objects.requireNonNull(filter, "filter"), null,
                false);
    }

    /** "Can't cast noncreature spells". */
    public static PlayerRestriction castingNoncreatureSpells() {
        return new PlayerRestriction(Kind.CAST_NONCREATURE_SPELLS, List.of());
    }

    /** "Can't play lands". */
    public static PlayerRestriction playingLands() {
        return new PlayerRestriction(Kind.PLAY_LANDS, List.of());
    }

    /** "Can't play cards from [zones]": lands and spells alike (CR 701.18b). */
    public static PlayerRestriction playingCardsFrom(Zone... zones) {
        return new PlayerRestriction(Kind.PLAY_CARDS, List.of(), null, EnumSet.copyOf(List.of(zones)), false);
    }

    /** "Can't activate abilities that aren't mana abilities". */
    public static PlayerRestriction activatingNonManaAbilities() {
        return new PlayerRestriction(Kind.ACTIVATE_NON_MANA_ABILITIES, List.of());
    }

    /** This restriction, only from {@code zones} ("can't cast spells from graveyards"). */
    public PlayerRestriction from(Zone... zones) {
        return new PlayerRestriction(kind, spellTypes, spellFilter, EnumSet.copyOf(List.of(zones)), false);
    }

    /** This restriction, from every zone but {@code zones} ("from anywhere other than their hands"). */
    public PlayerRestriction fromAnywhereBut(Zone... zones) {
        return new PlayerRestriction(kind, spellTypes, spellFilter, EnumSet.copyOf(List.of(zones)), true);
    }

    /** Whether this stops every spell, from every zone. */
    public boolean stopsEverySpell() {
        return kind == Kind.CAST_SPELLS && spellTypes.isEmpty() && spellFilter == null && zones == null;
    }

    /** Whether this restriction is limited to some zones. */
    public boolean isZoneBound() {
        return zones != null;
    }

    /** Whether this restriction covers an action taken from {@code zone}. */
    public boolean appliesFrom(Zone zone) {
        return zones == null || zones.contains(zone) != exceptZones;
    }

    /**
     * Whether this zone-bound restriction stops casting every spell from {@code zone} (lands aside, unless it stops
     * playing cards).
     */
    public boolean stopsSpellsFrom(Zone zone) {
        return zones != null && (kind == Kind.CAST_SPELLS || kind == Kind.PLAY_CARDS) && appliesFrom(zone);
    }

    /** Whether this zone-bound restriction stops casting a spell from {@code zone}, judged by whether it is a creature. */
    public boolean stopsSpellFrom(Zone zone, boolean creatureSpell) {
        return stopsSpellsFrom(zone)
                || zones != null && kind == Kind.CAST_NONCREATURE_SPELLS && !creatureSpell && appliesFrom(zone);
    }

    /** Whether this stops playing lands from {@code zone}: a land or card-play restriction covering that zone. */
    public boolean stopsLandsFrom(Zone zone) {
        return (kind == Kind.PLAY_LANDS || kind == Kind.PLAY_CARDS) && appliesFrom(zone);
    }

    /** The restricted action as a log line says it after "can't": "cast instant or sorcery spells", "play lands". */
    public String description() {
        String action = switch (kind) {
            case CAST_SPELLS -> spellFilter != null ? "cast " + spellsDescription(spellFilter)
                    : spellTypes.isEmpty() ? "cast spells" : "cast " + spellTypes.stream()
                    .map(type -> type.getDisplayName().toLowerCase())
                    .collect(Collectors.joining(" or ")) + " spells";
            case CAST_NONCREATURE_SPELLS -> "cast noncreature spells";
            case PLAY_LANDS -> zones == null ? "play lands" : "play land cards";
            case PLAY_CARDS -> "play cards";
            case ACTIVATE_NON_MANA_ABILITIES -> "activate abilities that aren't mana abilities";
        };
        if (zones == null) {
            return action;
        }
        String named = zones.stream().sorted().map(PlayerRestriction::zoneWord).collect(Collectors.joining(" or "));
        return exceptZones ? action + " from anywhere other than their " + named : action + " from their " + named;
    }

    private static String spellsDescription(CardPredicate filter) {
        if (filter instanceof CardAllOfPredicate(List<CardPredicate> parts) && parts.size() == 2
                && parts.get(0) instanceof CardMinManaValuePredicate(int min, boolean ignoredMinX)
                && parts.get(1) instanceof CardMaxManaValuePredicate(int max, boolean ignoredMaxX)
                && min == max) {
            return "spells with mana value " + min;
        }
        return CardPredicateUtils.describeFilter(filter).replaceFirst("card", "spells");
    }

    private static String zoneWord(Zone zone) {
        return switch (zone) {
            case HAND -> "hands";
            case GRAVEYARD -> "graveyards";
            case LIBRARY -> "libraries";
            case EXILE -> "exile";
            default -> zone.name().toLowerCase().replace('_', ' ');
        };
    }
}
