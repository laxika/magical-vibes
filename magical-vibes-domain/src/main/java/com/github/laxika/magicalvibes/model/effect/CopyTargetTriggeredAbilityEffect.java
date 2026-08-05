package com.github.laxika.magicalvibes.model.effect;

/**
 * Copies target triggered ability on the stack. The copy's controller may choose new targets for it
 * (CR 707.10). Pair with a {@code StackEntryPredicateTargetFilter} admitting
 * {@code TRIGGERED_ABILITY} (and typically {@code StackEntryControlledByPredicate} for "you control").
 *
 * <p>Used by Strionic Resonator.</p>
 */
public record CopyTargetTriggeredAbilityEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.spellOnStack());
    }
}
