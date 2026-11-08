package com.github.laxika.magicalvibes.model.effect;

/**
 * Transforms the permanent referenced by the stack entry's {@code targetId} (to its other face), or, with
 * {@code triggeringPermanent}, the entry's triggering permanent: "you may transform it" for the Werewolf that just
 * entered (Vildin-Pack Alpha), read as the ability resolves and not targeted. Non-transformable permanents (no back
 * face) are a no-op; transform-prevention is respected.
 *
 * @param triggeringPermanent whether "it" is the stack entry's triggering permanent rather than its {@code targetId}
 */
public record TransformTargetPermanentEffect(boolean triggeringPermanent) implements CardEffect {

    public TransformTargetPermanentEffect() {
        this(false);
    }

    /** "Transform it", "it" the stack entry's triggering permanent. */
    public static TransformTargetPermanentEffect ofTriggeringPermanent() {
        return new TransformTargetPermanentEffect(true);
    }

    @Override
    public TargetSpec targetSpec() {
        return triggeringPermanent ? TargetSpec.NONE : TargetSpec.benign(TargetPredicates.permanent());
    }
}
