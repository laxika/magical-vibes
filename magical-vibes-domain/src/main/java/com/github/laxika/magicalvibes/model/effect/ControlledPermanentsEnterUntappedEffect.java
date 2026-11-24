package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static replacement effect (CR 614.1c): permanents matching {@code filter} that {@code players} control enter
 * untapped. "Lands you control enter untapped" ({@link #lands()}: Horizon Explorer, Spelunking, The Wandering
 * Minstrel) is {@code you()}; "other permanents enter untapped" (Archelos, Lagoon Mystic, as the upgrade of a
 * {@link ConditionalReplacementEffect}) is {@code eachPlayer()}. The permanent having the ability isn't affected as it
 * enters, as it is not on the battlefield yet.
 *
 * @param filter  the permanents that enter untapped, judged as they enter
 * @param players whose entering permanents it applies to ({@code you()}, {@code eachPlayer()}), judged against the
 *                source and its controller
 */
public record ControlledPermanentsEnterUntappedEffect(PermanentPredicate filter, PlayerSubject players)
        implements ReplacementEffect {

    public ControlledPermanentsEnterUntappedEffect {
        if (players == null) {
            players = Subjects.you();
        }
    }

    /** Matching permanents you control enter untapped. */
    public ControlledPermanentsEnterUntappedEffect(PermanentPredicate filter) {
        this(filter, Subjects.you());
    }

    /** "Lands you control enter untapped." */
    public static ControlledPermanentsEnterUntappedEffect lands() {
        return new ControlledPermanentsEnterUntappedEffect(new PermanentIsLandPredicate(), Subjects.you());
    }

    /** Matching permanents enter untapped, whoever controls them. */
    public static ControlledPermanentsEnterUntappedEffect forEveryPlayer(PermanentPredicate filter) {
        return new ControlledPermanentsEnterUntappedEffect(filter, Subjects.eachPlayer());
    }
}
