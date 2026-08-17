package com.github.laxika.magicalvibes.model.effect;

/** Marks one or more permanents as saddled until end of turn. */
public record BecomeSaddledUntilEndOfTurnEffect(GrantScope scope) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case SELF -> new TargetSpec(null, false, null, true, 1);
            case TARGET -> TargetSpec.benign(TargetPredicates.permanent());
            default -> TargetSpec.NONE;
        };
    }
}
