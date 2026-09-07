package com.github.laxika.magicalvibes.model.effect;

/**
 * Shuffles the permanent enchanted by the source Aura into its owner's library.
 *
 * <p>The attached permanent is captured when the Aura ability is activated, so the effect can
 * still resolve against the same permanent if the Aura becomes unattached before resolution.</p>
 */
public record ShuffleEnchantedPermanentIntoOwnerLibraryEffect()
        implements AttachedPermanentSelfTargetingEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
