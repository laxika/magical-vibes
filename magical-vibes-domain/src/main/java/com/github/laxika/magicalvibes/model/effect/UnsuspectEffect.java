package com.github.laxika.magicalvibes.model.effect;

/** Removes the suspected designation from a permanent selected by the scope. */
public record UnsuspectEffect(GrantScope scope) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case TARGET -> TargetSpec.benign(TargetPredicates.creature());
            case SELF -> new TargetSpec(null, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
