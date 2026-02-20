package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class InteractionState {

    public AwaitingInput awaitingInput;
    public InteractionContext context;
    public UUID awaitingCardChoicePlayerId;
    public Set<Integer> awaitingCardChoiceValidIndices;
    public UUID awaitingPermanentChoicePlayerId;
    public Set<UUID> awaitingPermanentChoiceValidIds;
    public Card pendingAuraCard;
    public PermanentChoiceContext permanentChoiceContext;
    public UUID pendingCardChoiceTargetPermanentId;
    public UUID awaitingGraveyardChoicePlayerId;
    public Set<Integer> awaitingGraveyardChoiceValidIndices;
    public GraveyardChoiceDestination graveyardChoiceDestination;
    public List<Card> graveyardChoiceCardPool;
    public UUID awaitingColorChoicePlayerId;
    public UUID awaitingColorChoicePermanentId;
    public UUID pendingColorChoiceETBTargetId;
    public ColorChoiceContext colorChoiceContext;
    public UUID awaitingMayAbilityPlayerId;
    public UUID awaitingMultiPermanentChoicePlayerId;
    public Set<UUID> awaitingMultiPermanentChoiceValidIds;
    public int awaitingMultiPermanentChoiceMaxCount;
    public UUID awaitingMultiGraveyardChoicePlayerId;
    public Set<UUID> awaitingMultiGraveyardChoiceValidCardIds;
    public int awaitingMultiGraveyardChoiceMaxCount;
    public UUID awaitingLibraryReorderPlayerId;
    public List<Card> awaitingLibraryReorderCards;
    public boolean awaitingLibraryReorderToBottom;
    public UUID awaitingLibrarySearchPlayerId;
    public List<Card> awaitingLibrarySearchCards;
    public boolean awaitingLibrarySearchReveals;
    public boolean awaitingLibrarySearchCanFailToFind;
    public UUID awaitingLibrarySearchTargetPlayerId;
    public int awaitingLibrarySearchRemainingCount;
    public int awaitingDiscardRemainingCount;
    public UUID awaitingRevealedHandChoiceTargetPlayerId;
    public int awaitingRevealedHandChoiceRemainingCount;
    public final List<Card> awaitingRevealedHandChosenCards = new ArrayList<>();
    public boolean awaitingRevealedHandChoiceDiscardMode;
    public UUID awaitingHandTopBottomPlayerId;
    public List<Card> awaitingHandTopBottomCards;
    public UUID awaitingLibraryRevealPlayerId;
    public List<Card> awaitingLibraryRevealAllCards;
    public Set<UUID> awaitingLibraryRevealValidCardIds;

    public void clearContext() {
        this.context = null;
    }

    public boolean isAwaitingInput() {
        return this.awaitingInput != null;
    }

    public AwaitingInput awaitingInputType() {
        return this.awaitingInput;
    }

    public boolean isAwaitingInput(AwaitingInput inputType) {
        return this.awaitingInput == inputType;
    }

    public InteractionContext currentContext() {
        return this.context;
    }

    public void clearAwaitingInput() {
        this.awaitingInput = null;
        this.context = null;
    }

    public void beginAttackerDeclaration(UUID activePlayerId) {
        this.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;
        this.context = new InteractionContext.AttackerDeclaration(activePlayerId);
    }

    public void beginBlockerDeclaration(UUID defenderId) {
        this.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;
        this.context = new InteractionContext.BlockerDeclaration(defenderId);
    }

    public void beginCardChoice(AwaitingInput type, UUID playerId, Set<Integer> validIndices, UUID targetPermanentId) {
        this.awaitingInput = type;
        this.awaitingCardChoicePlayerId = playerId;
        this.awaitingCardChoiceValidIndices = new HashSet<>(validIndices);
        this.pendingCardChoiceTargetPermanentId = targetPermanentId;
        this.context = new InteractionContext.CardChoice(type, playerId, new HashSet<>(validIndices), targetPermanentId);
    }

    public void clearCardChoice() {
        this.awaitingCardChoicePlayerId = null;
        this.awaitingCardChoiceValidIndices = null;
        this.pendingCardChoiceTargetPermanentId = null;
    }

    public void beginPermanentChoice(UUID playerId, Set<UUID> validIds, PermanentChoiceContext choiceContext) {
        this.awaitingInput = AwaitingInput.PERMANENT_CHOICE;
        this.awaitingPermanentChoicePlayerId = playerId;
        this.awaitingPermanentChoiceValidIds = new HashSet<>(validIds);
        this.permanentChoiceContext = choiceContext;
        this.context = new InteractionContext.PermanentChoice(playerId, new HashSet<>(validIds), choiceContext);
    }

    public void clearPermanentChoice() {
        this.awaitingPermanentChoicePlayerId = null;
        this.awaitingPermanentChoiceValidIds = null;
        this.permanentChoiceContext = null;
    }

    public void beginGraveyardChoice(UUID playerId, Set<Integer> validIndices, GraveyardChoiceDestination destination, List<Card> cardPool) {
        this.awaitingInput = AwaitingInput.GRAVEYARD_CHOICE;
        this.awaitingGraveyardChoicePlayerId = playerId;
        this.awaitingGraveyardChoiceValidIndices = new HashSet<>(validIndices);
        this.graveyardChoiceDestination = destination;
        this.graveyardChoiceCardPool = cardPool;
        this.context = new InteractionContext.GraveyardChoice(playerId, new HashSet<>(validIndices), destination, cardPool);
    }

    public void clearGraveyardChoice() {
        this.awaitingGraveyardChoicePlayerId = null;
        this.awaitingGraveyardChoiceValidIndices = null;
        this.graveyardChoiceDestination = null;
        this.graveyardChoiceCardPool = null;
    }

    public void beginColorChoice(UUID playerId, UUID permanentId, UUID etbTargetPermanentId, ColorChoiceContext choiceContext) {
        this.awaitingInput = AwaitingInput.COLOR_CHOICE;
        this.awaitingColorChoicePlayerId = playerId;
        this.awaitingColorChoicePermanentId = permanentId;
        this.pendingColorChoiceETBTargetId = etbTargetPermanentId;
        this.colorChoiceContext = choiceContext;
        this.context = new InteractionContext.ColorChoice(playerId, permanentId, etbTargetPermanentId, choiceContext);
    }

    public void clearColorChoice() {
        this.awaitingColorChoicePlayerId = null;
        this.awaitingColorChoicePermanentId = null;
        this.pendingColorChoiceETBTargetId = null;
        this.colorChoiceContext = null;
    }

    public void beginMultiPermanentChoice(UUID playerId, Set<UUID> validIds, int maxCount) {
        this.awaitingInput = AwaitingInput.MULTI_PERMANENT_CHOICE;
        this.awaitingMultiPermanentChoicePlayerId = playerId;
        this.awaitingMultiPermanentChoiceValidIds = new HashSet<>(validIds);
        this.awaitingMultiPermanentChoiceMaxCount = maxCount;
        this.context = new InteractionContext.MultiPermanentChoice(playerId, new HashSet<>(validIds), maxCount);
    }

    public void clearMultiPermanentChoice() {
        this.awaitingMultiPermanentChoicePlayerId = null;
        this.awaitingMultiPermanentChoiceValidIds = null;
        this.awaitingMultiPermanentChoiceMaxCount = 0;
    }

    public void beginMultiGraveyardChoice(UUID playerId, Set<UUID> validCardIds, int maxCount) {
        this.awaitingInput = AwaitingInput.MULTI_GRAVEYARD_CHOICE;
        this.awaitingMultiGraveyardChoicePlayerId = playerId;
        this.awaitingMultiGraveyardChoiceValidCardIds = new HashSet<>(validCardIds);
        this.awaitingMultiGraveyardChoiceMaxCount = maxCount;
        this.context = new InteractionContext.MultiGraveyardChoice(playerId, new HashSet<>(validCardIds), maxCount);
    }

    public void clearMultiGraveyardChoice() {
        this.awaitingMultiGraveyardChoicePlayerId = null;
        this.awaitingMultiGraveyardChoiceValidCardIds = null;
        this.awaitingMultiGraveyardChoiceMaxCount = 0;
    }

    public void beginLibraryReorder(UUID playerId, List<Card> cards, boolean toBottom) {
        this.awaitingInput = AwaitingInput.LIBRARY_REORDER;
        this.awaitingLibraryReorderPlayerId = playerId;
        this.awaitingLibraryReorderCards = cards;
        this.awaitingLibraryReorderToBottom = toBottom;
        this.context = new InteractionContext.LibraryReorder(playerId, cards, toBottom);
    }

    public void clearLibraryReorder() {
        this.awaitingLibraryReorderPlayerId = null;
        this.awaitingLibraryReorderCards = null;
        this.awaitingLibraryReorderToBottom = false;
    }

    public void beginLibrarySearch(UUID playerId, List<Card> cards, boolean reveals, boolean canFailToFind,
                                   UUID targetPlayerId, int remainingCount) {
        this.awaitingInput = AwaitingInput.LIBRARY_SEARCH;
        this.awaitingLibrarySearchPlayerId = playerId;
        this.awaitingLibrarySearchCards = cards;
        this.awaitingLibrarySearchReveals = reveals;
        this.awaitingLibrarySearchCanFailToFind = canFailToFind;
        this.awaitingLibrarySearchTargetPlayerId = targetPlayerId;
        this.awaitingLibrarySearchRemainingCount = remainingCount;
        this.context = new InteractionContext.LibrarySearch(playerId, cards, reveals, canFailToFind, targetPlayerId, remainingCount);
    }

    public void clearLibrarySearch() {
        this.awaitingLibrarySearchPlayerId = null;
        this.awaitingLibrarySearchCards = null;
        this.awaitingLibrarySearchReveals = false;
        this.awaitingLibrarySearchCanFailToFind = false;
        this.awaitingLibrarySearchTargetPlayerId = null;
        this.awaitingLibrarySearchRemainingCount = 0;
    }

    public void beginLibraryRevealChoice(UUID playerId, List<Card> allCards, Set<UUID> validCardIds) {
        this.awaitingInput = AwaitingInput.LIBRARY_REVEAL_CHOICE;
        this.awaitingLibraryRevealPlayerId = playerId;
        this.awaitingLibraryRevealAllCards = allCards;
        this.awaitingLibraryRevealValidCardIds = validCardIds;
        this.context = new InteractionContext.LibraryRevealChoice(playerId, allCards, validCardIds);
    }

    public void clearLibraryRevealChoice() {
        this.awaitingLibraryRevealPlayerId = null;
        this.awaitingLibraryRevealAllCards = null;
        this.awaitingLibraryRevealValidCardIds = null;
    }

    public void beginHandTopBottomChoice(UUID playerId, List<Card> cards) {
        this.awaitingInput = AwaitingInput.HAND_TOP_BOTTOM_CHOICE;
        this.awaitingHandTopBottomPlayerId = playerId;
        this.awaitingHandTopBottomCards = cards;
        this.context = new InteractionContext.HandTopBottomChoice(playerId, cards);
    }

    public void clearHandTopBottomChoice() {
        this.awaitingHandTopBottomPlayerId = null;
        this.awaitingHandTopBottomCards = null;
    }

    public void beginMayAbilityChoice(UUID playerId, String description) {
        this.awaitingInput = AwaitingInput.MAY_ABILITY_CHOICE;
        this.awaitingMayAbilityPlayerId = playerId;
        this.context = new InteractionContext.MayAbilityChoice(playerId, description);
    }

    public void clearMayAbilityChoice() {
        this.awaitingMayAbilityPlayerId = null;
    }

    public void beginRevealedHandChoice(UUID choosingPlayerId, UUID targetPlayerId, Set<Integer> validIndices,
                                        int remainingCount, boolean discardMode, List<Card> chosenCards) {
        this.awaitingInput = AwaitingInput.REVEALED_HAND_CHOICE;
        this.awaitingCardChoicePlayerId = choosingPlayerId;
        this.awaitingCardChoiceValidIndices = new HashSet<>(validIndices);
        this.awaitingRevealedHandChoiceTargetPlayerId = targetPlayerId;
        this.awaitingRevealedHandChoiceRemainingCount = remainingCount;
        this.awaitingRevealedHandChoiceDiscardMode = discardMode;
        this.awaitingRevealedHandChosenCards.clear();
        if (chosenCards != null) {
            this.awaitingRevealedHandChosenCards.addAll(chosenCards);
        }
        this.context = new InteractionContext.RevealedHandChoice(
                choosingPlayerId,
                targetPlayerId,
                new HashSet<>(validIndices),
                remainingCount,
                discardMode,
                new ArrayList<>(this.awaitingRevealedHandChosenCards)
        );
    }

    public void beginRevealedHandChoiceFromCurrentState(UUID choosingPlayerId, UUID targetPlayerId, Set<Integer> validIndices) {
        List<Card> chosenCardsSnapshot = new ArrayList<>(this.awaitingRevealedHandChosenCards);
        beginRevealedHandChoice(
                choosingPlayerId,
                targetPlayerId,
                validIndices,
                this.awaitingRevealedHandChoiceRemainingCount,
                this.awaitingRevealedHandChoiceDiscardMode,
                chosenCardsSnapshot
        );
    }

    public void setDiscardRemainingCount(int remainingCount) {
        this.awaitingDiscardRemainingCount = Math.max(remainingCount, 0);
    }

    public int decrementDiscardRemainingCount() {
        if (this.awaitingDiscardRemainingCount > 0) {
            this.awaitingDiscardRemainingCount--;
        }
        return this.awaitingDiscardRemainingCount;
    }

    public int discardRemainingCount() {
        return this.awaitingDiscardRemainingCount;
    }

    public void addRevealedHandChosenCard(Card card) {
        this.awaitingRevealedHandChosenCards.add(card);
    }

    public int decrementRevealedHandChoiceRemainingCount() {
        if (this.awaitingRevealedHandChoiceRemainingCount > 0) {
            this.awaitingRevealedHandChoiceRemainingCount--;
        }
        return this.awaitingRevealedHandChoiceRemainingCount;
    }

    public int revealedHandChoiceRemainingCount() {
        return this.awaitingRevealedHandChoiceRemainingCount;
    }

    public boolean revealedHandChoiceDiscardMode() {
        return this.awaitingRevealedHandChoiceDiscardMode;
    }

    public List<Card> revealedHandChosenCardsSnapshot() {
        return new ArrayList<>(this.awaitingRevealedHandChosenCards);
    }

    public void clearRevealedHandChoiceProgress() {
        this.awaitingRevealedHandChoiceTargetPlayerId = null;
        this.awaitingRevealedHandChoiceRemainingCount = 0;
        this.awaitingRevealedHandChoiceDiscardMode = false;
        this.awaitingRevealedHandChosenCards.clear();
    }

    public InteractionContext.CardChoice cardChoiceContext() {
        if (context instanceof InteractionContext.CardChoice cc) return cc;
        if (awaitingCardChoicePlayerId == null || awaitingCardChoiceValidIndices == null || awaitingInput == null) return null;
        return new InteractionContext.CardChoice(awaitingInput, awaitingCardChoicePlayerId, awaitingCardChoiceValidIndices,
                pendingCardChoiceTargetPermanentId);
    }

    public InteractionContext.PermanentChoice permanentChoiceContextView() {
        if (context instanceof InteractionContext.PermanentChoice pc) return pc;
        if (awaitingPermanentChoicePlayerId == null || awaitingPermanentChoiceValidIds == null) return null;
        return new InteractionContext.PermanentChoice(awaitingPermanentChoicePlayerId, awaitingPermanentChoiceValidIds, permanentChoiceContext);
    }

    public InteractionContext.GraveyardChoice graveyardChoiceContext() {
        if (context instanceof InteractionContext.GraveyardChoice gc) return gc;
        if (awaitingGraveyardChoicePlayerId == null || awaitingGraveyardChoiceValidIndices == null) return null;
        return new InteractionContext.GraveyardChoice(awaitingGraveyardChoicePlayerId, awaitingGraveyardChoiceValidIndices,
                graveyardChoiceDestination, graveyardChoiceCardPool);
    }

    public InteractionContext.ColorChoice colorChoiceContextView() {
        if (context instanceof InteractionContext.ColorChoice cc) return cc;
        if (awaitingColorChoicePlayerId == null && colorChoiceContext == null) return null;
        return new InteractionContext.ColorChoice(awaitingColorChoicePlayerId, awaitingColorChoicePermanentId,
                pendingColorChoiceETBTargetId, colorChoiceContext);
    }

    public InteractionContext.MayAbilityChoice mayAbilityChoiceContext() {
        if (context instanceof InteractionContext.MayAbilityChoice mc) return mc;
        if (awaitingMayAbilityPlayerId == null) return null;
        return new InteractionContext.MayAbilityChoice(awaitingMayAbilityPlayerId, "");
    }

    public InteractionContext.MultiPermanentChoice multiPermanentChoiceContext() {
        if (context instanceof InteractionContext.MultiPermanentChoice mpc) return mpc;
        if (awaitingMultiPermanentChoicePlayerId == null || awaitingMultiPermanentChoiceValidIds == null) return null;
        return new InteractionContext.MultiPermanentChoice(awaitingMultiPermanentChoicePlayerId,
                awaitingMultiPermanentChoiceValidIds, awaitingMultiPermanentChoiceMaxCount);
    }

    public InteractionContext.MultiGraveyardChoice multiGraveyardChoiceContext() {
        if (context instanceof InteractionContext.MultiGraveyardChoice mgc) return mgc;
        if (awaitingMultiGraveyardChoicePlayerId == null || awaitingMultiGraveyardChoiceValidCardIds == null) return null;
        return new InteractionContext.MultiGraveyardChoice(awaitingMultiGraveyardChoicePlayerId,
                awaitingMultiGraveyardChoiceValidCardIds, awaitingMultiGraveyardChoiceMaxCount);
    }

    public InteractionContext.LibraryReorder libraryReorderContext() {
        if (context instanceof InteractionContext.LibraryReorder lr) return lr;
        if (awaitingLibraryReorderPlayerId == null || awaitingLibraryReorderCards == null) return null;
        return new InteractionContext.LibraryReorder(awaitingLibraryReorderPlayerId, awaitingLibraryReorderCards,
                awaitingLibraryReorderToBottom);
    }

    public InteractionContext.LibrarySearch librarySearchContext() {
        if (context instanceof InteractionContext.LibrarySearch ls) return ls;
        if (awaitingLibrarySearchPlayerId == null || awaitingLibrarySearchCards == null) return null;
        return new InteractionContext.LibrarySearch(awaitingLibrarySearchPlayerId, awaitingLibrarySearchCards,
                awaitingLibrarySearchReveals, awaitingLibrarySearchCanFailToFind,
                awaitingLibrarySearchTargetPlayerId, awaitingLibrarySearchRemainingCount);
    }

    public InteractionContext.LibraryRevealChoice libraryRevealChoiceContext() {
        if (context instanceof InteractionContext.LibraryRevealChoice lrc) return lrc;
        if (awaitingLibraryRevealPlayerId == null || awaitingLibraryRevealAllCards == null || awaitingLibraryRevealValidCardIds == null) return null;
        return new InteractionContext.LibraryRevealChoice(awaitingLibraryRevealPlayerId,
                awaitingLibraryRevealAllCards, awaitingLibraryRevealValidCardIds);
    }

    public InteractionContext.HandTopBottomChoice handTopBottomChoiceContext() {
        if (context instanceof InteractionContext.HandTopBottomChoice htbc) return htbc;
        if (awaitingHandTopBottomPlayerId == null || awaitingHandTopBottomCards == null) return null;
        return new InteractionContext.HandTopBottomChoice(awaitingHandTopBottomPlayerId, awaitingHandTopBottomCards);
    }

    public InteractionContext.RevealedHandChoice revealedHandChoiceContext() {
        if (context instanceof InteractionContext.RevealedHandChoice rhc) return rhc;
        if (awaitingCardChoicePlayerId == null || awaitingCardChoiceValidIndices == null || awaitingRevealedHandChoiceTargetPlayerId == null) return null;
        return new InteractionContext.RevealedHandChoice(awaitingCardChoicePlayerId, awaitingRevealedHandChoiceTargetPlayerId,
                awaitingCardChoiceValidIndices, awaitingRevealedHandChoiceRemainingCount,
                awaitingRevealedHandChoiceDiscardMode, awaitingRevealedHandChosenCards);
    }
}
