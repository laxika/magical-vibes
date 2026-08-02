package com.github.laxika.magicalvibes.model.effect;

/**
 * Offers Auras from the configured zones and attaches the chosen cards to the source permanent.
 * The no-argument form is Bruna's any-number battlefield/graveyard/hand effect; the parameterized
 * form also supports one-card searches that include the controller's library.
 */
public record AttachAurasToSourceEffect(boolean includeBattlefield, boolean includeLibrary,
                                        int maxCount) implements CardEffect {

    public AttachAurasToSourceEffect() {
        this(true, false, Integer.MAX_VALUE);
    }

    public AttachAurasToSourceEffect(boolean includeLibrary, int maxCount) {
        this(true, includeLibrary, maxCount);
    }

    public static AttachAurasToSourceEffect oneAuraSearch() {
        return new AttachAurasToSourceEffect(false, true, 1);
    }

    public AttachAurasToSourceEffect {
        if (maxCount < 1) {
            throw new IllegalArgumentException("maxCount must be positive");
        }
    }
}
