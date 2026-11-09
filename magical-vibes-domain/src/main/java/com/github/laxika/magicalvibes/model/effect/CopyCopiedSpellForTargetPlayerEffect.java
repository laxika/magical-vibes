package com.github.laxika.magicalvibes.model.effect;

/**
 * "Target opponent may also copy that spell" of a spell-copy trigger: the chosen player copies the stack entry's
 * triggering spell, the spell copy the trigger event was about, as it is on the stack or as it last existed there, and
 * may choose new targets for the copy.
 */
public record CopyCopiedSpellForTargetPlayerEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
