package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public sealed interface InteractionContext permits
        InteractionContext.AttackerDeclaration,
        InteractionContext.BlockerDeclaration,
        InteractionContext.CardChoice,
        InteractionContext.PermanentChoice,
        InteractionContext.GraveyardChoice,
        InteractionContext.ColorChoice,
        InteractionContext.MayAbilityChoice,
        InteractionContext.MultiPermanentChoice,
        InteractionContext.MultiGraveyardChoice,
        InteractionContext.LibraryReorder,
        InteractionContext.LibrarySearch,
        InteractionContext.LibraryRevealChoice,
        InteractionContext.HandTopBottomChoice,
        InteractionContext.RevealedHandChoice,
        InteractionContext.CombatDamageAssignment {

    record AttackerDeclaration(UUID activePlayerId) implements InteractionContext {}

    record BlockerDeclaration(UUID defenderId) implements InteractionContext {}

    record CardChoice(AwaitingInput type, UUID playerId, Set<Integer> validIndices, UUID targetPermanentId) implements InteractionContext {}

    record PermanentChoice(UUID playerId, Set<UUID> validIds, PermanentChoiceContext context) implements InteractionContext {}

    record GraveyardChoice(UUID playerId, Set<Integer> validIndices, GraveyardChoiceDestination destination, List<Card> cardPool) implements InteractionContext {}

    record ColorChoice(UUID playerId, UUID permanentId, UUID etbTargetPermanentId, ColorChoiceContext context) implements InteractionContext {}

    record MayAbilityChoice(UUID playerId, String description) implements InteractionContext {}

    record MultiPermanentChoice(UUID playerId, Set<UUID> validIds, int maxCount) implements InteractionContext {}

    record MultiGraveyardChoice(UUID playerId, Set<UUID> validCardIds, int maxCount) implements InteractionContext {}

    record LibraryReorder(UUID playerId, List<Card> cards, boolean toBottom) implements InteractionContext {}

    record LibrarySearch(UUID playerId, List<Card> cards, boolean reveals, boolean canFailToFind,
                         UUID targetPlayerId, int remainingCount, List<Card> sourceCards,
                         boolean reorderRemainingToBottom, boolean shuffleAfterSelection,
                         String prompt, LibrarySearchDestination destination) implements InteractionContext {}

    record LibraryRevealChoice(UUID playerId, List<Card> allCards, Set<UUID> validCardIds) implements InteractionContext {}

    record HandTopBottomChoice(UUID playerId, List<Card> cards) implements InteractionContext {}

    record RevealedHandChoice(UUID choosingPlayerId, UUID targetPlayerId, Set<Integer> validIndices,
                              int remainingCount, boolean discardMode, List<Card> chosenCards) implements InteractionContext {}

    record CombatDamageAssignment(UUID playerId, int attackerIndex, UUID attackerPermanentId,
                                   String attackerName, int totalDamage, List<CombatDamageTarget> validTargets,
                                   boolean isTrample) implements InteractionContext {}
}
