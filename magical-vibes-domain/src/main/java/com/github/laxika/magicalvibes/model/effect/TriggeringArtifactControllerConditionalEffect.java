package com.github.laxika.magicalvibes.model.effect;

/**
 * Trigger-only wrapper for effects that fire when an artifact controlled by the
 * triggering permanent's controller is put into a graveyard from the battlefield.
 */
public record TriggeringArtifactControllerConditionalEffect(CardEffect wrapped,
                                                             boolean onlyIfNotSacrificed,
                                                             boolean onlyIfNontoken)
        implements CardEffect {

    public TriggeringArtifactControllerConditionalEffect(CardEffect wrapped) {
        this(wrapped, false, false);
    }

    public TriggeringArtifactControllerConditionalEffect(CardEffect wrapped,
                                                         boolean onlyIfNotSacrificed) {
        this(wrapped, onlyIfNotSacrificed, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return wrapped.targetSpec();
    }
}
