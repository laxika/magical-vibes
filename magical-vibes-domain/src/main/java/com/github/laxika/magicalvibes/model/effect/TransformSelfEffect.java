package com.github.laxika.magicalvibes.model.effect;

/**
 * Transforms the source permanent to its back face, or back to its front face if already transformed.
 * Used by double-faced cards with the Transform keyword.
 */
public record TransformSelfEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        // Marks the ability as self-affecting so trigger collectors stamp sourcePermanentId
        // (needed when TransformSelf is nested under SpellCastTriggerEffect / SequenceEffect).
        return new TargetSpec(TargetCategory.NONE, false, null, true, 1);
    }
}
