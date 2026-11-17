package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Grants the controller permission to look at and play the top card of the target player's
 * library until end of turn, including permission to spend mana as though it were mana of any
 * color to cast spells played this way.
 *
 * <p>The floating effect it makes carries a copy of this record that names the library.
 *
 * @param libraryOwnerId the player whose library top may be used, or {@code null} on the effect a card is written with
 */
public record GrantControllerLibraryTopPermissionForTargetPlayerUntilEndOfTurnEffect(UUID libraryOwnerId)
        implements CardEffect {

    public GrantControllerLibraryTopPermissionForTargetPlayerUntilEndOfTurnEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
