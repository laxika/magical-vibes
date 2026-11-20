package com.github.laxika.magicalvibes.model.effect;

/** Removes the suspected designation from a permanent selected by the subject. */
public record UnsuspectEffect(PermanentSubject subject) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return switch (subject) {
            case PermanentSubject.Target ignored -> TargetSpec.benign(TargetPredicates.creature());
            case PermanentSubject.Source ignored -> new TargetSpec(null, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
