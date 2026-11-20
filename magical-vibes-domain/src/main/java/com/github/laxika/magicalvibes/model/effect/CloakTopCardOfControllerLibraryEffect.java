package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Each player {@code player} names puts the top card of their own library onto the battlefield cloaked. "For each
 * permanent exiled this way, its controller cloaks the top card of their library" after a one-per-player exile is
 * {@code new CloakTopCardOfControllerLibraryEffect(Subjects.playersThisWay(StepKind.EXILE))} (Unexplained Absence).
 *
 * @param player who cloaks from their own library
 */
public record CloakTopCardOfControllerLibraryEffect(PlayerSubject player) implements CardEffect {

    public CloakTopCardOfControllerLibraryEffect {
        Objects.requireNonNull(player, "player");
    }

    /** The effect's controller cloaks the top card of their library. */
    public CloakTopCardOfControllerLibraryEffect() {
        this(Subjects.you());
    }
}
