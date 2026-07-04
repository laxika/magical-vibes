package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

public sealed interface InteractionContext permits
        InteractionContext.AttackerDeclaration,
        InteractionContext.BlockerDeclaration,
        InteractionContext.CombatDamageAssignment {

    record AttackerDeclaration(UUID activePlayerId) implements InteractionContext {}

    record BlockerDeclaration(UUID defenderId) implements InteractionContext {}

    record CombatDamageAssignment(UUID playerId, int attackerIndex, UUID attackerPermanentId,
                                   String attackerName, int totalDamage, List<CombatDamageTarget> validTargets,
                                   boolean isTrample, boolean isDeathtouch) implements InteractionContext {}
}
