package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Parameter object for initiating a library search interaction.
 * Use the static {@link #builder(UUID, List)} method to construct instances.
 */
public record LibrarySearchParams(
        UUID playerId,
        List<Card> cards,
        boolean reveals,
        boolean canFailToFind,
        UUID targetPlayerId,
        int remainingCount,
        List<Card> sourceCards,
        boolean reorderRemainingToBottom,
        boolean reorderRemainingToTop,
        boolean shuffleAfterSelection,
        String prompt,
        LibrarySearchDestination destination,
        Set<CardType> filterCardTypes,
        List<Card> accumulatedCards,
        String filterCardName,
        UUID attachToPlayerId,
        CardPredicate filterPredicate
) {
    public static Builder builder(UUID playerId, List<Card> cards) {
        return new Builder(playerId, cards);
    }

    public static class Builder {
        private final UUID playerId;
        private final List<Card> cards;
        private boolean reveals;
        private boolean canFailToFind;
        private UUID targetPlayerId;
        private int remainingCount;
        private List<Card> sourceCards;
        private boolean reorderRemainingToBottom;
        private boolean reorderRemainingToTop;
        private boolean shuffleAfterSelection = true;
        private String prompt;
        private LibrarySearchDestination destination = LibrarySearchDestination.HAND;
        private Set<CardType> filterCardTypes;
        private List<Card> accumulatedCards = List.of();
        private String filterCardName;
        private UUID attachToPlayerId;
        private CardPredicate filterPredicate;

        private Builder(UUID playerId, List<Card> cards) {
            this.playerId = playerId;
            this.cards = cards;
        }

        public Builder reveals(boolean reveals) {
            this.reveals = reveals;
            return this;
        }

        public Builder canFailToFind(boolean canFailToFind) {
            this.canFailToFind = canFailToFind;
            return this;
        }

        public Builder targetPlayerId(UUID targetPlayerId) {
            this.targetPlayerId = targetPlayerId;
            return this;
        }

        public Builder remainingCount(int remainingCount) {
            this.remainingCount = remainingCount;
            return this;
        }

        public Builder sourceCards(List<Card> sourceCards) {
            this.sourceCards = sourceCards;
            return this;
        }

        public Builder reorderRemainingToBottom(boolean reorderRemainingToBottom) {
            this.reorderRemainingToBottom = reorderRemainingToBottom;
            return this;
        }

        public Builder reorderRemainingToTop(boolean reorderRemainingToTop) {
            this.reorderRemainingToTop = reorderRemainingToTop;
            return this;
        }

        public Builder shuffleAfterSelection(boolean shuffleAfterSelection) {
            this.shuffleAfterSelection = shuffleAfterSelection;
            return this;
        }

        public Builder prompt(String prompt) {
            this.prompt = prompt;
            return this;
        }

        public Builder destination(LibrarySearchDestination destination) {
            this.destination = destination;
            return this;
        }

        public Builder filterCardTypes(Set<CardType> filterCardTypes) {
            this.filterCardTypes = filterCardTypes;
            return this;
        }

        public Builder accumulatedCards(List<Card> accumulatedCards) {
            this.accumulatedCards = accumulatedCards;
            return this;
        }

        public Builder filterCardName(String filterCardName) {
            this.filterCardName = filterCardName;
            return this;
        }

        public Builder attachToPlayerId(UUID attachToPlayerId) {
            this.attachToPlayerId = attachToPlayerId;
            return this;
        }

        public Builder filterPredicate(CardPredicate filterPredicate) {
            this.filterPredicate = filterPredicate;
            return this;
        }

        public LibrarySearchParams build() {
            return new LibrarySearchParams(playerId, cards, reveals, canFailToFind, targetPlayerId,
                    remainingCount, sourceCards, reorderRemainingToBottom, reorderRemainingToTop,
                    shuffleAfterSelection, prompt, destination, filterCardTypes, accumulatedCards,
                    filterCardName, attachToPlayerId, filterPredicate);
        }
    }
}
