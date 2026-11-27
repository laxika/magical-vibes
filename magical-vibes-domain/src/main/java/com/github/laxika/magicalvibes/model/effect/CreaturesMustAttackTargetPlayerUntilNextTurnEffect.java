package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.Set;
import java.util.UUID;

/**
 * Floating combat requirement used for a creature that must attack a specified player each combat
 * if able. {@code targetPlayerId} may also name a planeswalker (Gideon, Battle-Forged's +2).
 */
public record CreaturesMustAttackTargetPlayerUntilNextTurnEffect(UUID targetPlayerId)
        implements CombatAttackRequirementEffect {

    private static final PermanentPredicate CREATURE = new PermanentIsCreaturePredicate();

    @Override
    public PermanentPredicate affectedPredicate() {
        return CREATURE;
    }

    @Override
    public Set<UUID> requiredAttackTargetIds(GameData gameData, Permanent sourcePermanent) {
        return targetPlayerId == null ? Set.of() : Set.of(targetPlayerId);
    }
}
