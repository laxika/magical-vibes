package com.github.laxika.magicalvibes.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class InteractionState {

    private AwaitingInput awaitingInput;
    private InteractionContext context;
    private UUID awaitingCardChoicePlayerId;
    private Set<Integer> awaitingCardChoiceValidIndices;
    private UUID awaitingPermanentChoicePlayerId;
    private Set<UUID> awaitingPermanentChoiceValidIds;
    private Card pendingAuraCard;
    private PermanentChoiceContext permanentChoiceContext;
    private UUID pendingCardChoiceTargetPermanentId;
    private UUID awaitingGraveyardChoicePlayerId;
    private Set<Integer> awaitingGraveyardChoiceValidIndices;
    private GraveyardChoiceDestination graveyardChoiceDestination;
    private List<Card> graveyardChoiceCardPool;
    private UUID awaitingColorChoicePlayerId;
    private UUID awaitingColorChoicePermanentId;
    private UUID pendingColorChoiceETBTargetId;
    private ColorChoiceContext colorChoiceContext;
    private UUID awaitingMayAbilityPlayerId;
    private UUID awaitingMultiPermanentChoicePlayerId;
    private Set<UUID> awaitingMultiPermanentChoiceValidIds;
    private int awaitingMultiPermanentChoiceMaxCount;
    private UUID awaitingMultiGraveyardChoicePlayerId;
    private Set<UUID> awaitingMultiGraveyardChoiceValidCardIds;
    private int awaitingMultiGraveyardChoiceMaxCount;
    private UUID awaitingLibraryReorderPlayerId;
    private List<Card> awaitingLibraryReorderCards;
    private boolean awaitingLibraryReorderToBottom;
    private UUID awaitingLibrarySearchPlayerId;
    private List<Card> awaitingLibrarySearchCards;
    private boolean awaitingLibrarySearchReveals;
    private boolean awaitingLibrarySearchCanFailToFind;
    private UUID awaitingLibrarySearchTargetPlayerId;
    private int awaitingLibrarySearchRemainingCount;
    private List<Card> awaitingLibrarySearchSourceCards;
    private boolean awaitingLibrarySearchReorderRemainingToBottom;
    private boolean awaitingLibrarySearchShuffleAfterSelection;
    private String awaitingLibrarySearchPrompt;
    private int awaitingDiscardRemainingCount;
    private UUID awaitingRevealedHandChoiceTargetPlayerId;
    private int awaitingRevealedHandChoiceRemainingCount;
    private final List<Card> awaitingRevealedHandChosenCards = new ArrayList<>();
    private boolean awaitingRevealedHandChoiceDiscardMode;
    private UUID awaitingHandTopBottomPlayerId;
    private List<Card> awaitingHandTopBottomCards;
    private UUID awaitingLibraryRevealPlayerId;
    private List<Card> awaitingLibraryRevealAllCards;
    private Set<UUID> awaitingLibraryRevealValidCardIds;

    public boolean isAwaitingInput() {
        return this.awaitingInput != null;
    }

    public AwaitingInput awaitingInputType() {
        return this.awaitingInput;
    }

    public void setAwaitingInput(AwaitingInput inputType) {
        this.awaitingInput = inputType;
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

    public UUID awaitingCardChoicePlayerId() {
        return this.awaitingCardChoicePlayerId;
    }

    public Set<Integer> awaitingCardChoiceValidIndices() {
        return this.awaitingCardChoiceValidIndices;
    }

    public UUID pendingCardChoiceTargetPermanentId() {
        return this.pendingCardChoiceTargetPermanentId;
    }

    public void beginPermanentChoice(UUID playerId, Set<UUID> validIds, PermanentChoiceContext choiceContext) {
        this.awaitingInput = AwaitingInput.PERMANENT_CHOICE;
        this.awaitingPermanentChoicePlayerId = playerId;
        this.awaitingPermanentChoiceValidIds = new HashSet<>(validIds);
        this.permanentChoiceContext = choiceContext;
        this.context = new InteractionContext.PermanentChoice(playerId, new HashSet<>(validIds), choiceContext);
    }

    public void setPermanentChoiceContext(PermanentChoiceContext choiceContext) {
        this.permanentChoiceContext = choiceContext;
    }

    public PermanentChoiceContext permanentChoiceContext() {
        return this.permanentChoiceContext;
    }

    public void clearPermanentChoiceContext() {
        this.permanentChoiceContext = null;
    }

    public void clearPermanentChoice() {
        this.awaitingPermanentChoicePlayerId = null;
        this.awaitingPermanentChoiceValidIds = null;
        this.permanentChoiceContext = null;
    }

    public UUID awaitingPermanentChoicePlayerId() {
        return this.awaitingPermanentChoicePlayerId;
    }

    public Set<UUID> awaitingPermanentChoiceValidIds() {
        return this.awaitingPermanentChoiceValidIds;
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

    public void prepareGraveyardChoice(GraveyardChoiceDestination destination, List<Card> cardPool) {
        this.graveyardChoiceDestination = destination;
        this.graveyardChoiceCardPool = cardPool;
    }

    public GraveyardChoiceDestination graveyardChoiceDestination() {
        return this.graveyardChoiceDestination;
    }

    public List<Card> graveyardChoiceCardPool() {
        return this.graveyardChoiceCardPool;
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

    public UUID awaitingColorChoicePlayerId() {
        return this.awaitingColorChoicePlayerId;
    }

    public UUID awaitingColorChoicePermanentId() {
        return this.awaitingColorChoicePermanentId;
    }

    public ColorChoiceContext colorChoiceContext() {
        return this.colorChoiceContext;
    }

    public void setPendingAuraCard(Card auraCard) {
        this.pendingAuraCard = auraCard;
    }

    public Card pendingAuraCard() {
        return this.pendingAuraCard;
    }

    public Card consumePendingAuraCard() {
        Card auraCard = this.pendingAuraCard;
        this.pendingAuraCard = null;
        return auraCard;
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

    public int awaitingMultiPermanentChoiceMaxCount() {
        return this.awaitingMultiPermanentChoiceMaxCount;
    }

    public UUID awaitingMultiPermanentChoicePlayerId() {
        return this.awaitingMultiPermanentChoicePlayerId;
    }

    public Set<UUID> awaitingMultiPermanentChoiceValidIds() {
        return this.awaitingMultiPermanentChoiceValidIds;
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

    public UUID awaitingMultiGraveyardChoicePlayerId() {
        return this.awaitingMultiGraveyardChoicePlayerId;
    }

    public Set<UUID> awaitingMultiGraveyardChoiceValidCardIds() {
        return this.awaitingMultiGraveyardChoiceValidCardIds;
    }

    public int awaitingMultiGraveyardChoiceMaxCount() {
        return this.awaitingMultiGraveyardChoiceMaxCount;
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

    public UUID awaitingLibraryReorderPlayerId() {
        return this.awaitingLibraryReorderPlayerId;
    }

    public List<Card> awaitingLibraryReorderCards() {
        return this.awaitingLibraryReorderCards;
    }

    public boolean awaitingLibraryReorderToBottom() {
        return this.awaitingLibraryReorderToBottom;
    }

    public void beginLibrarySearch(UUID playerId, List<Card> cards, boolean reveals, boolean canFailToFind,
                                   UUID targetPlayerId, int remainingCount) {
        beginLibrarySearch(playerId, cards, reveals, canFailToFind, targetPlayerId, remainingCount,
                null, false, true, null);
    }

    public void beginLibrarySearch(UUID playerId, List<Card> cards, boolean reveals, boolean canFailToFind,
                                   UUID targetPlayerId, int remainingCount, List<Card> sourceCards,
                                   boolean reorderRemainingToBottom, boolean shuffleAfterSelection, String prompt) {
        this.awaitingInput = AwaitingInput.LIBRARY_SEARCH;
        this.awaitingLibrarySearchPlayerId = playerId;
        this.awaitingLibrarySearchCards = cards;
        this.awaitingLibrarySearchReveals = reveals;
        this.awaitingLibrarySearchCanFailToFind = canFailToFind;
        this.awaitingLibrarySearchTargetPlayerId = targetPlayerId;
        this.awaitingLibrarySearchRemainingCount = remainingCount;
        this.awaitingLibrarySearchSourceCards = sourceCards;
        this.awaitingLibrarySearchReorderRemainingToBottom = reorderRemainingToBottom;
        this.awaitingLibrarySearchShuffleAfterSelection = shuffleAfterSelection;
        this.awaitingLibrarySearchPrompt = prompt;
        this.context = new InteractionContext.LibrarySearch(playerId, cards, reveals, canFailToFind,
                targetPlayerId, remainingCount, sourceCards, reorderRemainingToBottom, shuffleAfterSelection, prompt);
    }

    public void clearLibrarySearch() {
        this.awaitingLibrarySearchPlayerId = null;
        this.awaitingLibrarySearchCards = null;
        this.awaitingLibrarySearchReveals = false;
        this.awaitingLibrarySearchCanFailToFind = false;
        this.awaitingLibrarySearchTargetPlayerId = null;
        this.awaitingLibrarySearchRemainingCount = 0;
        this.awaitingLibrarySearchSourceCards = null;
        this.awaitingLibrarySearchReorderRemainingToBottom = false;
        this.awaitingLibrarySearchShuffleAfterSelection = true;
        this.awaitingLibrarySearchPrompt = null;
    }

    public UUID awaitingLibrarySearchPlayerId() {
        return this.awaitingLibrarySearchPlayerId;
    }

    public List<Card> awaitingLibrarySearchCards() {
        return this.awaitingLibrarySearchCards;
    }

    public boolean awaitingLibrarySearchReveals() {
        return this.awaitingLibrarySearchReveals;
    }

    public boolean awaitingLibrarySearchCanFailToFind() {
        return this.awaitingLibrarySearchCanFailToFind;
    }

    public UUID awaitingLibrarySearchTargetPlayerId() {
        return this.awaitingLibrarySearchTargetPlayerId;
    }

    public int awaitingLibrarySearchRemainingCount() {
        return this.awaitingLibrarySearchRemainingCount;
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

    public UUID awaitingHandTopBottomPlayerId() {
        return this.awaitingHandTopBottomPlayerId;
    }

    public List<Card> awaitingHandTopBottomCards() {
        return this.awaitingHandTopBottomCards;
    }

    public void beginMayAbilityChoice(UUID playerId, String description) {
        this.awaitingInput = AwaitingInput.MAY_ABILITY_CHOICE;
        this.awaitingMayAbilityPlayerId = playerId;
        this.context = new InteractionContext.MayAbilityChoice(playerId, description);
    }

    public void clearMayAbilityChoice() {
        this.awaitingMayAbilityPlayerId = null;
    }

    public UUID awaitingMayAbilityPlayerId() {
        return this.awaitingMayAbilityPlayerId;
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

    public boolean revealedHandChoiceDiscardMode() {
        return this.awaitingRevealedHandChoiceDiscardMode;
    }

    public int revealedHandChoiceRemainingCount() {
        return this.awaitingRevealedHandChoiceRemainingCount;
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
                awaitingLibrarySearchTargetPlayerId, awaitingLibrarySearchRemainingCount,
                awaitingLibrarySearchSourceCards, awaitingLibrarySearchReorderRemainingToBottom,
                awaitingLibrarySearchShuffleAfterSelection, awaitingLibrarySearchPrompt);
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
