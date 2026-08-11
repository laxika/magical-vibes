package com.github.laxika.magicalvibes.model.effect;

/**
 * Puts a target spell or creature on top of its owner's library, then lets that owner choose
 * whether it stays there or moves to the bottom instead.
 */
public record PutTargetSpellOrCreatureOnTopOrBottomOfLibraryEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.anyOf(
                TargetPredicates.creature(), TargetPredicates.spellOnStack()));
    }
}
