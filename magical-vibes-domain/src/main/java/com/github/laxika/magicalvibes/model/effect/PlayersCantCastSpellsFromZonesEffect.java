package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;

import java.util.Objects;
import java.util.Set;

/**
 * Static effect: the players {@code players} names can't do what {@code restriction} says, judged as the restriction is
 * checked: the static form of {@link PlayerRestrictionEffect}'s restriction.
 *
 * <ul>
 *   <li>"Players can't cast spells from graveyards [or libraries]" (Ashes of the Abhorrent, Grafdigger's Cage):
 *       {@code (eachPlayer(), castingSpells().from(zones))}; it stops flashback, graveyard casts, library-top casts and
 *       every other cast from those zones, never a land play (CR 305.1).</li>
 *   <li>"Your opponents can't cast spells from anywhere other than their hands" (Drannith Magistrate):
 *       {@code (eachOpponent(), castingSpells().fromAnywhereBut(HAND))}.</li>
 *   <li>"Players can't cast noncreature spells from graveyards or exile" (Soulless Jailer):
 *       {@code castingNoncreatureSpells().from(GRAVEYARD, EXILE)}.</li>
 *   <li>"You can't play lands" (Aggressive Mining, Rock Jockey under its condition): {@code (you(), playingLands())};
 *       "your opponents can't play land cards from graveyards" (Tomik): {@code playingLands().from(GRAVEYARD)};
 *       "you can't play lands or cast spells from your hand" (Experimental Frenzy):
 *       {@code (you(), playingCardsFrom(HAND))}. None of them stops a spell, unless it plays cards.</li>
 * </ul>
 *
 * <p>A face-down permanent or one that lost its abilities restricts nobody (CR 708.2a, 613.1f). Read by
 * {@code GameQueryService.isPlayerRestricted}, beside the resolved {@link PlayerRestrictionEffect}s.
 *
 * @param players     who is restricted: {@code you()}, {@code eachPlayer()} or {@code eachOpponent()}
 * @param restriction what they can't do; a spell restriction names its zones, and no types or filter
 */
public record PlayersCantCastSpellsFromZonesEffect(PlayerSubject players, PlayerRestriction restriction)
        implements CardEffect {

    public PlayersCantCastSpellsFromZonesEffect {
        Objects.requireNonNull(restriction, "restriction");
        if (!(players instanceof PlayerSubject.Each || players instanceof PlayerSubject.You)) {
            throw new IllegalArgumentException("Unsupported players for a static restriction: " + players);
        }
        boolean zoneFreeLands = restriction.kind() == PlayerRestriction.Kind.PLAY_LANDS && !restriction.isZoneBound();
        if (!zoneFreeLands && (!restriction.isZoneBound()
                || restriction.kind() == PlayerRestriction.Kind.ACTIVATE_NON_MANA_ABILITIES)) {
            throw new IllegalArgumentException("A static zone restriction names its zones: " + restriction);
        }
    }

    /** "Players can't cast spells from [zones]". */
    public PlayersCantCastSpellsFromZonesEffect(Set<Zone> zones) {
        this(zones, true);
    }

    /** "Players can't cast spells from [zones]", or only the source controller's opponents. */
    public PlayersCantCastSpellsFromZonesEffect(Set<Zone> zones, boolean appliesToAllPlayers) {
        this(appliesToAllPlayers ? Subjects.eachPlayer() : Subjects.eachOpponent(),
                PlayerRestriction.castingSpells().from(zones.toArray(Zone[]::new)));
    }
}
