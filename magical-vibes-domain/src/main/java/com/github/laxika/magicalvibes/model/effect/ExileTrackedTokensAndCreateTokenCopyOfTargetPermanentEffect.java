package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles tokens previously created with the source permanent, then creates a token copy of the
 * permanent referenced by the stack entry's target id.
 */
public record ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect(
        CreateTokenCopyOfTargetPermanentEffect copyEffect) implements CardEffect {

    public ExileTrackedTokensAndCreateTokenCopyOfTargetPermanentEffect() {
        this(new CreateTokenCopyOfTargetPermanentEffect());
    }

    @Override
    public TargetSpec targetSpec() {
        return copyEffect.targetSpec();
    }
}
