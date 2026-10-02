package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/** Offers eligible Auras, and optionally Equipment, from the configured zones and attaches the
 * chosen cards to the source permanent, or to {@code hostPermanentId} when supplied. */
public record AttachAurasToSourceEffect(boolean includeBattlefield, boolean includeLibrary,
                                        int maxCount, boolean includeEquipment,
                                        UUID hostPermanentId) implements CardEffect {

    public AttachAurasToSourceEffect() {
        this(true, false, Integer.MAX_VALUE, false, null);
    }

    public AttachAurasToSourceEffect(boolean includeLibrary, int maxCount) {
        this(true, includeLibrary, maxCount, false, null);
    }

    public AttachAurasToSourceEffect(boolean includeBattlefield, boolean includeLibrary, int maxCount) {
        this(includeBattlefield, includeLibrary, maxCount, false, null);
    }

    public AttachAurasToSourceEffect(boolean includeBattlefield, boolean includeLibrary,
                                     int maxCount, boolean includeEquipment) {
        this(includeBattlefield, includeLibrary, maxCount, includeEquipment, null);
    }

    public static AttachAurasToSourceEffect oneAuraSearch() {
        return new AttachAurasToSourceEffect(false, true, 1, false, null);
    }

    public static AttachAurasToSourceEffect oneAuraOrEquipmentFromHandOrGraveyard() {
        return new AttachAurasToSourceEffect(false, false, 1, true, null);
    }

    public AttachAurasToSourceEffect {
        if (maxCount < 1) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
    }
}
