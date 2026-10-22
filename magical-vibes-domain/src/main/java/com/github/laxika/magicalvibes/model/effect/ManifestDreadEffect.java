package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Manifests one of the top two cards of a player's library and puts the other into that player's
 * graveyard.
 *
 * @param useControllerLibrary whether to use the resolving effect's controller's library; when
 *                             {@code false}, the owner of a card exiled with the source permanent
 *                             is used unless {@code useTargetSpellControllerLibrary} is true
 * @param useTargetSpellControllerLibrary whether to use the controller of the spell targeted by
 *                                         the same resolving entry
 * @param libraryOwnerId an explicit player whose library is used, or {@code null}; set by the handler
 *                       when several owners of cards exiled with the source each manifest dread in turn
 */
public record ManifestDreadEffect(boolean useControllerLibrary, boolean useTargetSpellControllerLibrary,
                                 UUID libraryOwnerId)
        implements CardEffect {

    public ManifestDreadEffect() {
        this(false, false, null);
    }

    public static ManifestDreadEffect forController() {
        return new ManifestDreadEffect(true, false, null);
    }

    public static ManifestDreadEffect forTargetSpellController() {
        return new ManifestDreadEffect(false, true, null);
    }
}
