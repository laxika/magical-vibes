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
    public boolean canTargetPermanent() {
        return scope == PutOnTopOfLibraryScope.TARGET;
    }
}
