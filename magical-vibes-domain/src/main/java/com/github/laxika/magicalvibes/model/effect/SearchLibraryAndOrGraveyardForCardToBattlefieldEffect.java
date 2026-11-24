package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.EnumSet;
import java.util.Objects;
import java.util.Set;

/**
 * "Search your library and/or graveyard for a [filter] card and put it onto the battlefield. If you search your library
 * this way, shuffle": one matching permanent card the controller owns, chosen across {@code zones} (their graveyard,
 * hand and library), onto the battlefield. Only a permanent card is offered (CR 400.4a). A card in a hand or library
 * may be left unfound (CR 701.23b); only a find in the library searches and shuffles it.
 *
 * @param zones the zones searched: any of {@link Zone#GRAVEYARD}, {@link Zone#HAND} and {@link Zone#LIBRARY}
 * @param attachToSource whether the found permanent is attached to the source permanent
 * @param attachAuraOrEquipment whether an Aura is placed with a legal attachment choice and an
 *                              Equipment gets an optional attachment follow-up
 * @param enterWithCounters optional as-enters counters for the found permanent
 */
public record SearchLibraryAndOrGraveyardForCardToBattlefieldEffect(
        CardPredicate filter,
        Set<Zone> zones,
        boolean attachToSource,
        boolean attachAuraOrEquipment,
        ManaValueBound manaValueBound,
        EnterWithCountersEffect enterWithCounters
) implements CardEffect {

    private static final Set<Zone> SEARCHABLE = EnumSet.of(Zone.GRAVEYARD, Zone.HAND, Zone.LIBRARY);

    public SearchLibraryAndOrGraveyardForCardToBattlefieldEffect {
        zones = Set.copyOf(Objects.requireNonNull(zones, "zones"));
        if (zones.isEmpty() || !SEARCHABLE.containsAll(zones)) {
            throw new IllegalArgumentException("A card is put onto the battlefield from a graveyard, a hand or a"
                    + " library: " + zones);
        }
    }

    public SearchLibraryAndOrGraveyardForCardToBattlefieldEffect(
            CardPredicate filter, boolean includeHand, boolean includeLibrary,
            boolean attachToSource, boolean attachAuraOrEquipment,
            ManaValueBound manaValueBound, EnterWithCountersEffect enterWithCounters) {
        this(filter, zones(includeHand, includeLibrary), attachToSource, attachAuraOrEquipment, manaValueBound,
                enterWithCounters);
    }

    public SearchLibraryAndOrGraveyardForCardToBattlefieldEffect(CardPredicate filter) {
        this(filter, false, true, false, false, null, null);
    }

    public SearchLibraryAndOrGraveyardForCardToBattlefieldEffect(
            CardPredicate filter, boolean includeHand, boolean attachToSource) {
        this(filter, includeHand, true, attachToSource, false, null, null);
    }

    public SearchLibraryAndOrGraveyardForCardToBattlefieldEffect(
            CardPredicate filter, boolean includeHand, boolean includeLibrary,
            boolean attachToSource, boolean attachAuraOrEquipment) {
        this(filter, includeHand, includeLibrary, attachToSource, attachAuraOrEquipment, null, null);
    }

    /** Backward-compatible full constructor without the extended attachment mode. */
    public SearchLibraryAndOrGraveyardForCardToBattlefieldEffect(
            CardPredicate filter, boolean includeHand, boolean attachToSource,
            ManaValueBound manaValueBound, EnterWithCountersEffect enterWithCounters) {
        this(filter, includeHand, true, attachToSource, false, manaValueBound, enterWithCounters);
    }

    public SearchLibraryAndOrGraveyardForCardToBattlefieldEffect(
            CardPredicate filter, ManaValueBound manaValueBound) {
        this(filter, false, true, false, false, manaValueBound, null);
    }

    public SearchLibraryAndOrGraveyardForCardToBattlefieldEffect(
            CardPredicate filter, ManaValueBound manaValueBound,
            EnterWithCountersEffect enterWithCounters) {
        this(filter, false, true, false, false, manaValueBound, enterWithCounters);
    }

    /** "Search your hand and/or library for a [filter] card and put it onto the battlefield": only these zones. */
    public static SearchLibraryAndOrGraveyardForCardToBattlefieldEffect inZones(CardPredicate filter, Zone... zones) {
        return new SearchLibraryAndOrGraveyardForCardToBattlefieldEffect(filter, Set.of(zones), false, false, null,
                null);
    }

    private static Set<Zone> zones(boolean includeHand, boolean includeLibrary) {
        Set<Zone> zones = EnumSet.of(Zone.GRAVEYARD);
        if (includeHand) {
            zones.add(Zone.HAND);
        }
        if (includeLibrary) {
            zones.add(Zone.LIBRARY);
        }
        return zones;
    }
}
