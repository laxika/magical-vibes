package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts one or two permanents on top of their owners' libraries. {@code TARGET} (the default, chosen
 * target permanent), {@code SELF} (the source permanent, e.g. an Aura's own activated ability) or
 * {@code SELF_AND_TARGET} (both the source and the chosen target creature, followed by the
 * "then those players shuffle their libraries" clause — Void Stalker).
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
        return switch (scope) {
            case TARGET -> TargetSpec.benign(TargetCategory.PERMANENT);
            case SELF_AND_TARGET -> TargetSpec.benign(TargetCategory.CREATURE);
            case SELF -> TargetSpec.NONE;
        };
    }
}
