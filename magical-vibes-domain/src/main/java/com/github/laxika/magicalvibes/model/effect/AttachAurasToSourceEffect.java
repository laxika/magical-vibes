package com.github.laxika.magicalvibes.model.effect;

/** Offers eligible Auras, and optionally Equipment, from the configured zones and attaches the
 * chosen cards to the source permanent. */
public record AttachAurasToSourceEffect(boolean includeBattlefield, boolean includeLibrary,
                                        int maxCount, boolean includeEquipment) implements CardEffect {

    public AttachAurasToSourceEffect() {
        this(true, false, Integer.MAX_VALUE, false);
    }

    public AttachAurasToSourceEffect(boolean includeLibrary, int maxCount) {
        this(true, includeLibrary, maxCount, false);
    }

    public AttachAurasToSourceEffect(boolean includeBattlefield, boolean includeLibrary, int maxCount) {
        this(includeBattlefield, includeLibrary, maxCount, false);
    }

    public static AttachAurasToSourceEffect oneAuraSearch() {
        return new AttachAurasToSourceEffect(false, true, 1, false);
    }

    public static AttachAurasToSourceEffect oneAuraOrEquipmentFromHandOrGraveyard() {
        return new AttachAurasToSourceEffect(false, false, 1, true);
    }

    public AttachAurasToSourceEffect {
        if (maxCount < 1) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
    }
}
