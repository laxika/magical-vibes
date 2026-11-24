package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Each player {@code player} names puts the top card of their own library onto the battlefield cloaked. "For each
 * permanent exiled this way, its controller cloaks the top card of their library" after a one-per-player exile is
 * {@code new CloakTopCardOfControllerLibraryEffect(Subjects.playersThisWay(StepKind.EXILE))} (Unexplained Absence).
 *
 * <p>"You cloak the top card of that player's library" names the library apart ({@code libraryOwner}): the cloaked
 * permanent is the cloaking player's, whoever owns the card (Etrata, Deadly Fugitive).
 *
 * @param player       who cloaks
 * @param libraryOwner whose library the card comes from, or {@code null} for each cloaking player's own
 */
public record CloakTopCardOfControllerLibraryEffect(PlayerSubject player, PlayerSubject libraryOwner)
        implements CardEffect {

    public CloakTopCardOfControllerLibraryEffect {
        Objects.requireNonNull(player, "player");
    }

    /** Each player {@code player} names cloaks the top card of their own library. */
    public CloakTopCardOfControllerLibraryEffect(PlayerSubject player) {
        this(player, null);
    }

    /** "You cloak the top card of that player's library." */
    public static CloakTopCardOfControllerLibraryEffect fromLibraryOf(PlayerSubject libraryOwner) {
        return new CloakTopCardOfControllerLibraryEffect(Subjects.you(), Objects.requireNonNull(libraryOwner));
    }

    /** The effect's controller cloaks the top card of their library. */
    public CloakTopCardOfControllerLibraryEffect() {
        this(Subjects.you());
    }
}
