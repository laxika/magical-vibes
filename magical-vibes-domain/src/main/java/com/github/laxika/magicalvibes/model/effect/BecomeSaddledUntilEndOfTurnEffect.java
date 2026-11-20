package com.github.laxika.magicalvibes.model.effect;

/** Marks one or more permanents as saddled until end of turn. */
public record BecomeSaddledUntilEndOfTurnEffect(PermanentSubject subject) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return switch (subject) {
            case PermanentSubject.Source ignored -> new TargetSpec(null, false, null, true, 1);
            case PermanentSubject.Target ignored -> TargetSpec.benign(TargetPredicates.permanent());
            default -> TargetSpec.NONE;
        };
    }
}
