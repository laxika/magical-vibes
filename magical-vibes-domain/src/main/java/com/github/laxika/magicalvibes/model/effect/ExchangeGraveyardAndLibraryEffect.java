package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Zone;

import java.util.Objects;
import java.util.Set;

/**
 * "Exchange your [zone] and [zone]": the controller's cards in each of the two zones move to the other at once
 * (library, graveyard, hand). Nothing is drawn or discarded; a token in the graveyard stays behind (CR 111.7);
 * when two or more cards enter the graveyard together the owner orders them (CR 404.3). It doesn't shuffle: "then
 * shuffle" is a following {@link ShuffleLibraryEffect}.
 *
 * @param first  one exchanged zone
 * @param second the other
 */
public record ExchangeGraveyardAndLibraryEffect(Zone first, Zone second) implements CardEffect {

    private static final Set<Zone> EXCHANGEABLE = Set.of(Zone.LIBRARY, Zone.GRAVEYARD, Zone.HAND);

    public ExchangeGraveyardAndLibraryEffect {
        Objects.requireNonNull(first, "first");
        Objects.requireNonNull(second, "second");
        if (!EXCHANGEABLE.contains(first) || !EXCHANGEABLE.contains(second) || first == second) {
            throw new IllegalArgumentException("Exchanges two of library, graveyard and hand: " + first + ", " + second);
        }
    }

    /** "Exchange your graveyard and library" (Morality Shift). */
    public ExchangeGraveyardAndLibraryEffect() {
        this(Zone.GRAVEYARD, Zone.LIBRARY);
    }
}
