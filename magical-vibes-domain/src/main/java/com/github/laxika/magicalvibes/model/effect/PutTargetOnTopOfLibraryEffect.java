package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts a permanent on top of its owner's library. {@code TARGET} (the default, chosen target
 * permanent) or {@code SELF} (the source permanent, e.g. an Aura's own activated ability).
 */
public record PutTargetOnTopOfLibraryEffect(PutOnTopOfLibraryScope scope) implements CardEffect {

    public PutTargetOnTopOfLibraryEffect() {
        this(PutOnTopOfLibraryScope.TARGET);
    }

    public static PutTargetOnTopOfLibraryEffect self() {
        return new PutTargetOnTopOfLibraryEffect(PutOnTopOfLibraryScope.SELF);
    }

    @Override
    public TargetSpec targetSpec() {
        // Only the TARGET scope targets a permanent; the SELF scope targets the source and needs no
        // pipeline target. The kept validator still reads canTargetPermanent() (derived from this
        // spec) until step 10 rewrites the reader.
        return scope == PutOnTopOfLibraryScope.TARGET
                ? TargetSpec.benign(TargetCategory.PERMANENT)
                : TargetSpec.NONE;
    }
}
