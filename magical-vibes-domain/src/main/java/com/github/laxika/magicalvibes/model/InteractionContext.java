package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

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
        InteractionContext.MultiZoneExileChoice,
        InteractionContext.CombatDamageAssignment,
        InteractionContext.XValueChoice,
        InteractionContext.Scry,
        InteractionContext.KnowledgePoolCastChoice,
        InteractionContext.MirrorOfFateChoice {

    record AttackerDeclaration(UUID activePlayerId) implements InteractionContext {}

    record BlockerDeclaration(UUID defenderId) implements InteractionContext {}

    record CardChoice(AwaitingInput type, UUID playerId, Set<Integer> validIndices, UUID targetId) implements InteractionContext {}

    record PermanentChoice(UUID playerId, Set<UUID> validIds, PermanentChoiceContext context) implements InteractionContext {}

    record GraveyardChoice(UUID playerId, Set<Integer> validIndices, GraveyardChoiceDestination destination, List<Card> cardPool) implements InteractionContext {}

    record ColorChoice(UUID playerId, UUID permanentId, UUID etbTargetId, ChoiceContext context) implements InteractionContext {}

    record MayAbilityChoice(UUID playerId, String description) implements InteractionContext {}

    record MultiPermanentChoice(UUID playerId, Set<UUID> validIds, int maxCount) implements InteractionContext {}

    record MultiGraveyardChoice(UUID playerId, Set<UUID> validCardIds, int maxCount) implements InteractionContext {}

    record LibraryReorder(UUID playerId, List<Card> cards, boolean toBottom, UUID deckOwnerId) implements InteractionContext {

        public LibraryReorder(UUID playerId, List<Card> cards, boolean toBottom) {
            this(playerId, cards, toBottom, playerId);
        }
    }

    record LibrarySearch(UUID playerId, List<Card> cards, boolean reveals, boolean canFailToFind,
                         UUID targetPlayerId, int remainingCount, List<Card> sourceCards,
                         boolean reorderRemainingToBottom, boolean reorderRemainingToTop,
                         boolean shuffleAfterSelection,
                         String prompt, LibrarySearchDestination destination,
                         Set<CardType> filterCardTypes, List<Card> accumulatedCards,
                         String filterCardName, UUID attachToPlayerId,
                         CardPredicate filterPredicate) implements InteractionContext {

        public LibrarySearch(UUID playerId, List<Card> cards, boolean reveals, boolean canFailToFind,
                             UUID targetPlayerId, int remainingCount, List<Card> sourceCards,
                             boolean reorderRemainingToBottom, boolean shuffleAfterSelection,
                             String prompt, LibrarySearchDestination destination) {
            this(playerId, cards, reveals, canFailToFind, targetPlayerId, remainingCount, sourceCards,
                    reorderRemainingToBottom, false, shuffleAfterSelection, prompt, destination, null, List.of(), null, null, null);
        }
    }

    record LibraryRevealChoice(UUID playerId, List<Card> allCards, Set<UUID> validCardIds,
                               boolean remainingToGraveyard, boolean selectedToHand,
                               boolean reorderRemainingToBottom) implements InteractionContext {

        public LibraryRevealChoice(UUID playerId, List<Card> allCards, Set<UUID> validCardIds, boolean remainingToGraveyard) {
            this(playerId, allCards, validCardIds, remainingToGraveyard, false, false);
        }
    }

    record HandTopBottomChoice(UUID playerId, List<Card> cards) implements InteractionContext {}

    record RevealedHandChoice(UUID choosingPlayerId, UUID targetPlayerId, Set<Integer> validIndices,
                              int remainingCount, boolean discardMode, boolean exileMode, List<Card> chosenCards) implements InteractionContext {}

    record MultiZoneExileChoice(UUID playerId, Set<UUID> validCardIds, int maxCount, UUID targetPlayerId, UUID controllerId, String cardName) implements InteractionContext {}

    record CombatDamageAssignment(UUID playerId, int attackerIndex, UUID attackerPermanentId,
                                   String attackerName, int totalDamage, List<CombatDamageTarget> validTargets,
                                   boolean isTrample, boolean isDeathtouch) implements InteractionContext {}

    record XValueChoice(UUID playerId, int maxValue, String prompt, String cardName) implements InteractionContext {}

    record Scry(UUID playerId, List<Card> cards) implements InteractionContext {}

    record KnowledgePoolCastChoice(UUID playerId, Set<UUID> validCardIds, int maxCount) implements InteractionContext {}

    record MirrorOfFateChoice(UUID playerId, Set<UUID> validCardIds, int maxCount) implements InteractionContext {}
}
