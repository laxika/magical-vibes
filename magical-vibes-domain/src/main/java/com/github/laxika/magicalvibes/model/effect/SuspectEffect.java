package com.github.laxika.magicalvibes.model.effect;

/** Gives a creature the suspected designation. */
public record SuspectEffect(GrantScope scope) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case TARGET -> TargetSpec.harmful(TargetPredicates.creature());
            case SELF -> new TargetSpec(null, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
