package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.UUID;

/**
 * Artifact graveyard trigger: the targeted player chooses whether the source deals damage to them
 * instead of returning the triggering artifact to its owner's hand.
 */
public record ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect(
        int damage,
        UUID triggeringArtifactId
) implements DamageDealingEffect {

    public ReturnTriggeringArtifactToOwnerHandUnlessTargetTakesDamageEffect(int damage) {
        this(damage, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.player());
    }

    @Override
    public DynamicAmount damageAmount() {
        return new Fixed(damage);
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }
}
