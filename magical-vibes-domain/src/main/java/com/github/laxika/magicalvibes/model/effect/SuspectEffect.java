package com.github.laxika.magicalvibes.model.effect;

/** Gives a creature the suspected designation. */
public record SuspectEffect(PermanentSubject subject) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return switch (subject) {
            case PermanentSubject.Target ignored -> TargetSpec.harmful(TargetPredicates.creature());
            case PermanentSubject.Source ignored -> new TargetSpec(null, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
