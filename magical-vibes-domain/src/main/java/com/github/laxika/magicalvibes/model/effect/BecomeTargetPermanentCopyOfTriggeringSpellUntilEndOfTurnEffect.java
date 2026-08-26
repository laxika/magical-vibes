package com.github.laxika.magicalvibes.model.effect;

/**
 * Makes the targeted permanent a copy of the permanent spell that caused the trigger until end of
 * turn.
 */
public record BecomeTargetPermanentCopyOfTriggeringSpellUntilEndOfTurnEffect()
        implements TriggeringSpellReferencingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
