package com.github.laxika.magicalvibes.model.effect;

/**
 * Target creature can block {@code additionalBlocks} more creatures this turn (beyond its base one).
 * A one-shot, until-end-of-turn version of the static {@link GrantAdditionalBlockEffect}: the grant
 * is stored on the target {@link com.github.laxika.magicalvibes.model.Permanent}'s
 * {@code additionalBlocksUntilEndOfTurn} counter and cleared at end of turn. Used by Act of Heroism.
 */
public record GrantAdditionalBlockToTargetUntilEndOfTurnEffect(int additionalBlocks) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE);
    }
}
