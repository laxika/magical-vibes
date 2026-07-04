package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public sealed interface InteractionContext permits
        InteractionContext.AttackerDeclaration,
        InteractionContext.BlockerDeclaration,
        InteractionContext.PermanentChoice,
        InteractionContext.CombatDamageAssignment {

    record AttackerDeclaration(UUID activePlayerId) implements InteractionContext {}

    record BlockerDeclaration(UUID defenderId) implements InteractionContext {}
    record PermanentChoice(UUID playerId, Set<UUID> validIds, PermanentChoiceContext context) implements InteractionContext {}

    record CombatDamageAssignment(UUID playerId, int attackerIndex, UUID attackerPermanentId,
                                   String attackerName, int totalDamage, List<CombatDamageTarget> validTargets,
                                   boolean isTrample, boolean isDeathtouch) implements InteractionContext {}
}
