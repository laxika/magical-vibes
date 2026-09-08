package com.github.laxika.magicalvibes.model.effect;

/**
 * A death trigger that returns the triggering creature to its owner's hand unless its targeted
 * player pays life. The death-trigger collector supplies {@code dyingCardId} when the trigger is
 * collected, because the creature has already left the battlefield by resolution time.
 */
public record ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect(int lifeCost, java.util.UUID dyingCardId)
        implements CardEffect {

    public ReturnDyingCreatureToOwnerHandUnlessTargetPaysLifeEffect(int lifeCost) {
        this(lifeCost, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }
}
