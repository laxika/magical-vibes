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
 * from some zones, activating abilities (every ability, or those of permanents of some card types, mana abilities
 * included), or activating abilities that aren't mana abilities. Built through the factories; each reader in the
 * engine answers one of these questions.
 *
 * <p>A spell, land or card-play restriction may name the zones it applies to ({@link #from}, {@link #fromAnywhereBut}):
 * "can't cast spells from anywhere other than their hands" (Avatar's Wrath, Drannith Magistrate), "can't play land
 * cards from graveyards" (Tomik), "can't cast noncreature spells from graveyards or exile" (Soulless Jailer). A
 * restriction with zones names no types and no filter, and a restriction with a filter names no types. A zone-free,
 * type-free, filter-free spell restriction stops every spell ({@link #stopsEverySpell()}); the readers of a zone-free
 * kind ({@code GameData.isRestricted}) don't see a zone-bound one. With {@link #ofTheirOwn} the zones are the
 * restricted player's own ("can't cast spells from their graveyards", Shaman's Trance): a card in another player's
 * graveyard isn't covered, and with {@code exceptZones} only the player's own zones stay allowed ("anywhere other than
 * their hands"). A reader that doesn't know the card judges the player's own zone.
 *
 * @param kind        which action is restricted
 * @param spellTypes  for {@link Kind#CAST_SPELLS}, the card types of the spells that can't be cast, in the order the card
 *                    names them ("instant or sorcery"); empty for every spell, and for every other kind
 * @param spellFilter for {@link Kind#CAST_SPELLS}, which spells can't be cast ("spells with the same mana value as that
 *                    spell", fixed as the effect resolves); {@code null} for every spell
 * @param zones       the zones the action is restricted from, or with {@code exceptZones} the zones it is still allowed
 *                    from; {@code null} for every zone
 * @param exceptZones whether {@code zones} names the zones that stay allowed
 * @param abilitySourceTypes for {@link Kind#ACTIVATE_ABILITIES}, the card types of the permanents whose abilities can't
 *                    be activated ("abilities of artifacts, creatures, or enchantments": permanents only), in the order
 *                    the card names them; empty for every ability in every zone, and for every other kind
 * @param ownZones    whether {@code zones} are the restricted player's own zones ("their graveyards")
 */
public record PlayerRestriction(Kind kind, List<CardType> spellTypes, CardPredicate spellFilter, Set<Zone> zones,
                                boolean exceptZones, List<CardType> abilitySourceTypes, boolean ownZones) {

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
        ACTIVATE_NON_MANA_ABILITIES,
        /**
         * "Can't activate abilities [of {@link #abilitySourceTypes}]": mana abilities included, so a mana ability can't
         * be activated even during a payment (CR 605.3a gives no exception to a "can't", CR 101.2).
         */
        ACTIVATE_ABILITIES
    }

    public PlayerRestriction {
        Objects.requireNonNull(kind, "kind");
        spellTypes = List.copyOf(spellTypes);
        abilitySourceTypes = List.copyOf(abilitySourceTypes);
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
        if (kind != Kind.ACTIVATE_ABILITIES && !abilitySourceTypes.isEmpty()) {
            throw new IllegalArgumentException("Only an ability restriction names ability source types: " + kind);
        }
        if (new HashSet<>(abilitySourceTypes).size() != abilitySourceTypes.size()) {
            throw new IllegalArgumentException("An ability source type is named once: " + abilitySourceTypes);
        }
        if (zones != null && (zones.isEmpty() || kind == Kind.ACTIVATE_NON_MANA_ABILITIES
                || kind == Kind.ACTIVATE_ABILITIES || !spellTypes.isEmpty() || spellFilter != null)) {
            throw new IllegalArgumentException("Only a plain spell, land or card-play restriction names zones: " + kind);
        }
        if (kind == Kind.PLAY_CARDS && zones == null) {
            throw new IllegalArgumentException("A card-play restriction names its zones");
        }
        if (exceptZones && zones == null) {
            throw new IllegalArgumentException("No zones to except");
        }
        if (ownZones && zones == null) {
            throw new IllegalArgumentException("No zones to own");
        }
    }

    /** A restriction whose zones, if any, are every player's. */
    public PlayerRestriction(Kind kind, List<CardType> spellTypes, CardPredicate spellFilter, Set<Zone> zones,
                             boolean exceptZones, List<CardType> abilitySourceTypes) {
        this(kind, spellTypes, spellFilter, zones, exceptZones, abilitySourceTypes, false);
    }

    /** A restriction naming no ability source types. */
    public PlayerRestriction(Kind kind, List<CardType> spellTypes, CardPredicate spellFilter, Set<Zone> zones,
                             boolean exceptZones) {
        this(kind, spellTypes, spellFilter, zones, exceptZones, List.of());
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

    /** "Can't activate abilities": every activated ability in every zone, mana abilities included. */
    public static PlayerRestriction activatingAbilities() {
        return new PlayerRestriction(Kind.ACTIVATE_ABILITIES, List.of());
    }

    /**
     * "Can't activate abilities of [type] or [type]": the activated abilities of permanents of those card types, mana
     * abilities included, the types in the order the card names them.
     */
    public static PlayerRestriction activatingAbilitiesOf(CardType... types) {
        if (types.length == 0) {
            throw new IllegalArgumentException("Name at least one card type, or use activatingAbilities()");
        }
        return new PlayerRestriction(Kind.ACTIVATE_ABILITIES, List.of(), null, null, false, List.of(types));
    }

    /** This restriction, only from {@code zones} ("can't cast spells from graveyards"). */
    public PlayerRestriction from(Zone... zones) {
        return new PlayerRestriction(kind, spellTypes, spellFilter, EnumSet.copyOf(List.of(zones)), false,
                abilitySourceTypes);
    }

    /** This restriction, from every zone but {@code zones} ("from anywhere other than their hands"). */
    public PlayerRestriction fromAnywhereBut(Zone... zones) {
        return new PlayerRestriction(kind, spellTypes, spellFilter, EnumSet.copyOf(List.of(zones)), true,
                abilitySourceTypes);
    }

    /** This zone-bound restriction, its zones being the restricted player's own ("from their graveyards"). */
    public PlayerRestriction ofTheirOwn() {
        return new PlayerRestriction(kind, spellTypes, spellFilter, zones, exceptZones, abilitySourceTypes, true);
    }

    /** Whether this stops every spell, from every zone. */
    public boolean stopsEverySpell() {
        return kind == Kind.CAST_SPELLS && spellTypes.isEmpty() && spellFilter == null && zones == null;
    }

    /** Whether this restriction is limited to some zones. */
    public boolean isZoneBound() {
        return zones != null;
    }

    /** Whether this restriction covers an action taken from the restricted player's own {@code zone}. */
    public boolean appliesFrom(Zone zone) {
        return appliesFrom(zone, true);
    }

    /**
     * Whether this restriction covers an action taken from {@code zone}, {@code ownZone} telling whether the card is in
     * the restricted player's own zone of that kind (a graveyard, hand or library of theirs).
     */
    public boolean appliesFrom(Zone zone, boolean ownZone) {
        return zones == null || (zones.contains(zone) && (ownZone || !ownZones)) != exceptZones;
    }

    /**
     * Whether this zone-bound restriction stops casting every spell from the player's own {@code zone} (lands aside,
     * unless it stops playing cards).
     */
    public boolean stopsSpellsFrom(Zone zone) {
        return stopsSpellsFrom(zone, true);
    }

    /** As {@link #stopsSpellsFrom(Zone)}, {@code ownZone} telling whether the zone is the player's own. */
    public boolean stopsSpellsFrom(Zone zone, boolean ownZone) {
        return zones != null && (kind == Kind.CAST_SPELLS || kind == Kind.PLAY_CARDS) && appliesFrom(zone, ownZone);
    }

    /**
     * Whether this zone-bound restriction stops casting a spell from {@code zone}, judged by whether it is a creature and
     * whether the zone is the player's own.
     */
    public boolean stopsSpellFrom(Zone zone, boolean ownZone, boolean creatureSpell) {
        return stopsSpellsFrom(zone, ownZone)
                || zones != null && kind == Kind.CAST_NONCREATURE_SPELLS && !creatureSpell
                && appliesFrom(zone, ownZone);
    }

    /**
     * Whether this stops playing lands from the player's own {@code zone}: a land or card-play restriction covering
     * that zone.
     */
    public boolean stopsLandsFrom(Zone zone) {
        return stopsLandsFrom(zone, true);
    }

    /** As {@link #stopsLandsFrom(Zone)}, {@code ownZone} telling whether the zone is the player's own. */
    public boolean stopsLandsFrom(Zone zone, boolean ownZone) {
        return (kind == Kind.PLAY_LANDS || kind == Kind.PLAY_CARDS) && appliesFrom(zone, ownZone);
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
            case ACTIVATE_ABILITIES -> abilitySourceTypes.isEmpty() ? "activate abilities"
                    : "activate abilities of " + pluralList(abilitySourceTypes);
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

    private static String pluralList(List<CardType> types) {
        List<String> words = types.stream().map(type -> type.getDisplayName().toLowerCase() + "s").toList();
        if (words.size() == 1) {
            return words.getFirst();
        }
        String last = words.getLast();
        return String.join(", ", words.subList(0, words.size() - 1)) + (words.size() > 2 ? ", or " : " or ") + last;
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
