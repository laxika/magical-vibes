package com.github.laxika.magicalvibes.model.effect;

/**
 * Target creature can block {@code additionalBlocks} more creatures this turn (beyond its base one).
 * A one-shot, until-end-of-turn version of the static {@link GrantAdditionalBlockEffect}: the grant
 * is stored on the target {@link com.github.laxika.magicalvibes.model.Permanent}'s
 * {@code additionalBlocksUntilEndOfTurn} counter and cleared at end of turn. Used by Act of Heroism.
 *
 * <p>With {@link GrantScope#SELF} the grant applies to the source permanent instead of a target,
 * for self-pumping activated abilities such as Mounted Archers' {@code {W}}.
 */
public record GrantAdditionalBlockToTargetUntilEndOfTurnEffect(int additionalBlocks, GrantScope scope)
        implements CardEffect {

    public GrantAdditionalBlockToTargetUntilEndOfTurnEffect(int additionalBlocks) {
        this(additionalBlocks, GrantScope.TARGET);
    }

    @Override
    public TargetSpec targetSpec() {
        return scope == GrantScope.TARGET ? TargetSpec.benign(TargetCategory.CREATURE) : TargetSpec.NONE;
    }
}
