package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AnimatePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
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
        boolean restToGraveyard,
        boolean restToExile,
        boolean shuffleAfterSelection,
        String prompt,
        LibrarySearchDestination destination,
        Integer discoverValue,
        Set<CardType> filterCardTypes,
        List<Card> accumulatedCards,
        String filterCardName,
        UUID attachToPlayerId,
        UUID attachToPermanentId,
        CardPredicate filterPredicate,
        UUID sourcePermanentId,
        LibrarySearchFollowUp followUp,
        boolean requireDifferentNames,
        Integer manaValueBoundValue,
        boolean manaValueExact,
        List<String> excludedCardNames,
        boolean grantHaste,
        boolean exileAtEndStep,
        boolean returnToHandAtEndStep,
        AnimatePermanentsEffect animateFound,
        CounterType battlefieldCounter,
        boolean repeatUntilDecline,
        CreateTokenEffect tokenTemplate,
        String sourceSetCode,
        boolean sourceSideboard,
        CardSubtype battlefieldIfChosenBeholdType,
        Integer battlefieldIfManaValueAtMost,
        boolean placeBattlefieldCardsSimultaneously,
        boolean allowCastFromLibraryWhileSearching
) {
    public LibrarySearchParams {
        if (followUp == null) {
            followUp = LibrarySearchFollowUp.NONE;
        }
        if (excludedCardNames == null) {
            excludedCardNames = List.of();
        } else {
            excludedCardNames = List.copyOf(excludedCardNames);
        }
    }

    public static Builder builder(UUID playerId, List<Card> cards) {
        return new Builder(playerId, cards);
    }

    /** Returns a copy with a different searchable card list (all other fields unchanged). */
    public LibrarySearchParams withCards(List<Card> newCards) {
        return new LibrarySearchParams(playerId, newCards, reveals, canFailToFind, targetPlayerId,
                remainingCount, sourceCards, reorderRemainingToBottom, reorderRemainingToTop,
                restToGraveyard, restToExile, shuffleAfterSelection, prompt, destination, discoverValue, filterCardTypes,
                accumulatedCards, filterCardName, attachToPlayerId, attachToPermanentId,
                filterPredicate, sourcePermanentId, followUp, requireDifferentNames,
                manaValueBoundValue, manaValueExact, excludedCardNames, grantHaste, exileAtEndStep,
                returnToHandAtEndStep, animateFound, battlefieldCounter, repeatUntilDecline, tokenTemplate, sourceSetCode, sourceSideboard,
                battlefieldIfChosenBeholdType, battlefieldIfManaValueAtMost,
                placeBattlefieldCardsSimultaneously, allowCastFromLibraryWhileSearching);
    }

    public LibrarySearchParams withAllowCastFromLibraryWhileSearching(boolean allow) {
        return new LibrarySearchParams(playerId, cards, reveals, canFailToFind, targetPlayerId,
                remainingCount, sourceCards, reorderRemainingToBottom, reorderRemainingToTop,
                restToGraveyard, restToExile, shuffleAfterSelection, prompt, destination, discoverValue, filterCardTypes,
                accumulatedCards, filterCardName, attachToPlayerId, attachToPermanentId,
                filterPredicate, sourcePermanentId, followUp, requireDifferentNames,
                manaValueBoundValue, manaValueExact, excludedCardNames, grantHaste, exileAtEndStep,
                returnToHandAtEndStep, animateFound, battlefieldCounter, repeatUntilDecline, tokenTemplate, sourceSetCode,
                sourceSideboard, battlefieldIfChosenBeholdType, battlefieldIfManaValueAtMost,
                placeBattlefieldCardsSimultaneously, allow);
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
        private boolean restToGraveyard;
        private boolean restToExile;
        private boolean shuffleAfterSelection = true;
        private String prompt;
        private LibrarySearchDestination destination = LibrarySearchDestination.HAND;
        private Integer discoverValue;
        private Set<CardType> filterCardTypes;
        private List<Card> accumulatedCards = List.of();
        private String filterCardName;
        private UUID attachToPlayerId;
        private UUID attachToPermanentId;
        private CardPredicate filterPredicate;
        private UUID sourcePermanentId;
        private LibrarySearchFollowUp followUp = LibrarySearchFollowUp.NONE;
        private boolean requireDifferentNames;
        private Integer manaValueBoundValue;
        private boolean manaValueExact;
        private List<String> excludedCardNames = List.of();
        private boolean grantHaste;
        private boolean exileAtEndStep;
        private boolean returnToHandAtEndStep;
        private AnimatePermanentsEffect animateFound;
        private CounterType battlefieldCounter;
        private boolean repeatUntilDecline;
        private CreateTokenEffect tokenTemplate;
        private String sourceSetCode;
        private boolean sourceSideboard;
        private CardSubtype battlefieldIfChosenBeholdType;
        private Integer battlefieldIfManaValueAtMost;
        private boolean placeBattlefieldCardsSimultaneously;
        private boolean allowCastFromLibraryWhileSearching;

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

        public Builder restToGraveyard(boolean restToGraveyard) {
            this.restToGraveyard = restToGraveyard;
            return this;
        }

        public Builder restToExile(boolean restToExile) {
            this.restToExile = restToExile;
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

        public Builder discoverValue(Integer discoverValue) {
            this.discoverValue = discoverValue;
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

        public Builder attachToPermanentId(UUID attachToPermanentId) {
            this.attachToPermanentId = attachToPermanentId;
            return this;
        }

        public Builder filterPredicate(CardPredicate filterPredicate) {
            this.filterPredicate = filterPredicate;
            return this;
        }

        public Builder sourcePermanentId(UUID sourcePermanentId) {
            this.sourcePermanentId = sourcePermanentId;
            return this;
        }

        public Builder followUp(LibrarySearchFollowUp followUp) {
            this.followUp = followUp;
            return this;
        }

        public Builder requireDifferentNames(boolean requireDifferentNames) {
            this.requireDifferentNames = requireDifferentNames;
            return this;
        }

        public Builder manaValueBound(Integer manaValueBoundValue, boolean manaValueExact) {
            this.manaValueBoundValue = manaValueBoundValue;
            this.manaValueExact = manaValueExact;
            return this;
        }

        public Builder excludedCardNames(List<String> excludedCardNames) {
            this.excludedCardNames = excludedCardNames == null ? List.of() : excludedCardNames;
            return this;
        }

        public Builder grantHaste(boolean grantHaste) {
            this.grantHaste = grantHaste;
            return this;
        }

        public Builder exileAtEndStep(boolean exileAtEndStep) {
            this.exileAtEndStep = exileAtEndStep;
            return this;
        }

        public Builder returnToHandAtEndStep(boolean returnToHandAtEndStep) {
            this.returnToHandAtEndStep = returnToHandAtEndStep;
            return this;
        }

        public Builder animateFound(AnimatePermanentsEffect animateFound) {
            this.animateFound = animateFound;
            return this;
        }

        public Builder battlefieldCounter(CounterType battlefieldCounter) {
            this.battlefieldCounter = battlefieldCounter;
            return this;
        }

        public Builder repeatUntilDecline(boolean repeatUntilDecline) {
            this.repeatUntilDecline = repeatUntilDecline;
            return this;
        }

        public Builder tokenTemplate(CreateTokenEffect tokenTemplate) {
            this.tokenTemplate = tokenTemplate;
            return this;
        }

        public Builder sourceSetCode(String sourceSetCode) {
            this.sourceSetCode = sourceSetCode;
            return this;
        }

        public Builder sourceSideboard(boolean sourceSideboard) {
            this.sourceSideboard = sourceSideboard;
            return this;
        }

        public Builder allowCastFromLibraryWhileSearching(boolean allowCastFromLibraryWhileSearching) {
            this.allowCastFromLibraryWhileSearching = allowCastFromLibraryWhileSearching;
            return this;
        }

        public Builder battlefieldIfChosenBeholdType(CardSubtype battlefieldIfChosenBeholdType) {
            this.battlefieldIfChosenBeholdType = battlefieldIfChosenBeholdType;
            return this;
        }

        public Builder battlefieldIfManaValueAtMost(Integer battlefieldIfManaValueAtMost) {
            this.battlefieldIfManaValueAtMost = battlefieldIfManaValueAtMost;
            return this;
        }

        /** Holds selected battlefield cards until a bounded multi-pick flow completes. */
        public Builder placeBattlefieldCardsSimultaneously(boolean placeBattlefieldCardsSimultaneously) {
            this.placeBattlefieldCardsSimultaneously = placeBattlefieldCardsSimultaneously;
            return this;
        }

        public LibrarySearchParams build() {
            return new LibrarySearchParams(playerId, cards, reveals, canFailToFind, targetPlayerId,
                    remainingCount, sourceCards, reorderRemainingToBottom, reorderRemainingToTop,
                    restToGraveyard, restToExile, shuffleAfterSelection, prompt, destination, discoverValue, filterCardTypes,
                    accumulatedCards, filterCardName, attachToPlayerId, attachToPermanentId,
                    filterPredicate, sourcePermanentId, followUp, requireDifferentNames,
                    manaValueBoundValue, manaValueExact, excludedCardNames, grantHaste, exileAtEndStep,
                    returnToHandAtEndStep, animateFound, battlefieldCounter, repeatUntilDecline, tokenTemplate, sourceSetCode, sourceSideboard,
                    battlefieldIfChosenBeholdType, battlefieldIfManaValueAtMost,
                    placeBattlefieldCardsSimultaneously, allowCastFromLibraryWhileSearching);
        }
    }
}
