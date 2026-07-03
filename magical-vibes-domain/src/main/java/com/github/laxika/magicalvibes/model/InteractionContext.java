package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;
import java.util.Set;
import java.util.UUID;

public sealed interface InteractionContext permits
        InteractionContext.AttackerDeclaration,
        InteractionContext.BlockerDeclaration,
        InteractionContext.PermanentChoice,
        InteractionContext.LibrarySearch,
        InteractionContext.CombatDamageAssignment {

    record AttackerDeclaration(UUID activePlayerId) implements InteractionContext {}

    record BlockerDeclaration(UUID defenderId) implements InteractionContext {}
    record PermanentChoice(UUID playerId, Set<UUID> validIds, PermanentChoiceContext context) implements InteractionContext {}

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

    record CombatDamageAssignment(UUID playerId, int attackerIndex, UUID attackerPermanentId,
                                   String attackerName, int totalDamage, List<CombatDamageTarget> validTargets,
                                   boolean isTrample, boolean isDeathtouch) implements InteractionContext {}
}
