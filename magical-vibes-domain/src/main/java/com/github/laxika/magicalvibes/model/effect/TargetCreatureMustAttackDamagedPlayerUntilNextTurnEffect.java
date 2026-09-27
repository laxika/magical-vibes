package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/**
 * Makes the targeted creature attack the player dealt combat damage by the trigger each combat
 * if able, through the end of the ability controller's next turn.
 */
public record TargetCreatureMustAttackDamagedPlayerUntilNextTurnEffect(UUID damagedPlayerId)
        implements CardEffect, CombatAttackRequirementEffect {

    private static final PermanentPredicate CREATURE = new PermanentIsCreaturePredicate();

    public TargetCreatureMustAttackDamagedPlayerUntilNextTurnEffect() {
        this(null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }

    @Override
    public PermanentPredicate affectedPredicate() {
        return CREATURE;
    }

    @Override
    public UUID requiredAttackTargetId(GameData gameData, Permanent sourcePermanent) {
        return damagedPlayerId;
    }
}
