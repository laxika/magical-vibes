package com.github.laxika.magicalvibes.model.effect;

/** Puts the permanent enchanted by the source Aura into its owner's library at a fixed position from the top. */
public record PutEnchantedPermanentIntoLibraryNFromTopEffect(int position)
        implements AttachedPermanentSelfTargetingEffect {

    @Override
    public TargetSpec targetSpec() {
        return new TargetSpec(null, false, null, true, 1);
    }
}
