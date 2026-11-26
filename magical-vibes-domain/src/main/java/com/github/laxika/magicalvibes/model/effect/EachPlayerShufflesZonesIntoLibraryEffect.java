package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;

import java.util.Objects;
import java.util.Set;

/**
 * Each player {@code players} names shuffles the named zones into their library: their hand, their graveyard and, for
 * {@link Zone#BATTLEFIELD}, every permanent they own across all battlefields. The step records how many cards and
 * permanents each of them shuffled away ({@code StepKind.SHUFFLE_INTO_LIBRARY}): "then draws that many cards" is a
 * {@link DrawCardEffect#eachEvaluatedFor} of {@code AmountThisWay.shuffledAwayByYou()} over {@code eachPlayer()} (The
 * Great Aurora).
 *
 * <p>The no-argument form is the Timetwister family (Time Reversal, Diminishing Returns, Memory,
 * Jace, the Living Guildpact). {@code new EachPlayerShufflesZonesIntoLibraryEffect(true)} adds the
 * permanent sweep, which is Sway of the Stars. Pair either with a
 * {@code DrawCardEffect(7, eachPlayer())} for the "then draws seven cards" half. An empty zone's library is shuffled all
 * the same (CR 701.24d).
 *
 * @param players who shuffles; never a target
 * @param zones   what they shuffle in, of hand, graveyard and battlefield
 */
public record EachPlayerShufflesZonesIntoLibraryEffect(PlayerSubject players, Set<Zone> zones) implements CardEffect {

    private static final Set<Zone> SHUFFLABLE = Set.of(Zone.HAND, Zone.GRAVEYARD, Zone.BATTLEFIELD);

    public EachPlayerShufflesZonesIntoLibraryEffect {
        Objects.requireNonNull(players, "players");
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("The players who shuffle are not targeted: " + players);
        }
        zones = Set.copyOf(zones);
        if (zones.isEmpty() || !SHUFFLABLE.containsAll(zones)) {
            throw new IllegalArgumentException("A hand, a graveyard or owned permanents are shuffled in: " + zones);
        }
    }

    /** Hand and graveyard only (Timetwister family). */
    public EachPlayerShufflesZonesIntoLibraryEffect() {
        this(false);
    }

    /** Each player's hand and graveyard, with their owned permanents when {@code includeOwnedPermanents}. */
    public EachPlayerShufflesZonesIntoLibraryEffect(boolean includeOwnedPermanents) {
        this(Subjects.eachPlayer(), includeOwnedPermanents
                ? Set.of(Zone.HAND, Zone.GRAVEYARD, Zone.BATTLEFIELD)
                : Set.of(Zone.HAND, Zone.GRAVEYARD));
    }

    /** "Each player shuffles their graveyard into their library." */
    public static EachPlayerShufflesZonesIntoLibraryEffect graveyards() {
        return new EachPlayerShufflesZonesIntoLibraryEffect(Subjects.eachPlayer(), Set.of(Zone.GRAVEYARD));
    }

    /** "Each player shuffles all cards from their hand and all permanents they own into their library." */
    public static EachPlayerShufflesZonesIntoLibraryEffect handsAndOwnedPermanents() {
        return new EachPlayerShufflesZonesIntoLibraryEffect(Subjects.eachPlayer(), Set.of(Zone.HAND, Zone.BATTLEFIELD));
    }

    /** "[Players] shuffle the cards from their hand into their library." */
    public static EachPlayerShufflesZonesIntoLibraryEffect handsOf(PlayerSubject players) {
        return new EachPlayerShufflesZonesIntoLibraryEffect(players, Set.of(Zone.HAND));
    }

    /** "Each other player also shuffles their hand and graveyard into their library." */
    public static EachPlayerShufflesZonesIntoLibraryEffect handsAndGraveyardsOf(PlayerSubject players) {
        return new EachPlayerShufflesZonesIntoLibraryEffect(players, Set.of(Zone.HAND, Zone.GRAVEYARD));
    }
}
