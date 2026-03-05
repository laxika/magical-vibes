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
    private boolean graveyardChoiceGainLifeEqualToManaValue;
    private UUID graveyardChoiceAttachToSourcePermanentId;
    private UUID pendingEquipmentAttachEquipmentId;
    private UUID pendingEquipmentAttachTargetId;
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
    private LibrarySearchDestination awaitingLibrarySearchDestination = LibrarySearchDestination.HAND;
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
    private UUID awaitingMultiZoneExileChoicePlayerId;
    private Set<UUID> awaitingMultiZoneExileChoiceValidCardIds;
    private int awaitingMultiZoneExileChoiceMaxCount;

    /**
     * Creates a deep copy of this interaction state for AI simulation.
     * Immutable references (records, enums, UUIDs, Strings) are shared.
     * Mutable collections are copied to new instances.
     */
    public InteractionState deepCopy() {
        InteractionState copy = new InteractionState();
        copy.awaitingInput = this.awaitingInput;
        copy.context = this.context; // sealed interface of records — immutable
        copy.awaitingCardChoicePlayerId = this.awaitingCardChoicePlayerId;
        copy.awaitingCardChoiceValidIndices = this.awaitingCardChoiceValidIndices != null ? new HashSet<>(this.awaitingCardChoiceValidIndices) : null;
        copy.awaitingPermanentChoicePlayerId = this.awaitingPermanentChoicePlayerId;
        copy.awaitingPermanentChoiceValidIds = this.awaitingPermanentChoiceValidIds != null ? new HashSet<>(this.awaitingPermanentChoiceValidIds) : null;
        copy.pendingAuraCard = this.pendingAuraCard;
        copy.permanentChoiceContext = this.permanentChoiceContext; // sealed interface of records — immutable
        copy.pendingCardChoiceTargetPermanentId = this.pendingCardChoiceTargetPermanentId;
        copy.awaitingGraveyardChoicePlayerId = this.awaitingGraveyardChoicePlayerId;
        copy.awaitingGraveyardChoiceValidIndices = this.awaitingGraveyardChoiceValidIndices != null ? new HashSet<>(this.awaitingGraveyardChoiceValidIndices) : null;
        copy.graveyardChoiceDestination = this.graveyardChoiceDestination;
        copy.graveyardChoiceCardPool = this.graveyardChoiceCardPool != null ? new ArrayList<>(this.graveyardChoiceCardPool) : null;
        copy.graveyardChoiceGainLifeEqualToManaValue = this.graveyardChoiceGainLifeEqualToManaValue;
        copy.graveyardChoiceAttachToSourcePermanentId = this.graveyardChoiceAttachToSourcePermanentId;
        copy.pendingEquipmentAttachEquipmentId = this.pendingEquipmentAttachEquipmentId;
        copy.pendingEquipmentAttachTargetId = this.pendingEquipmentAttachTargetId;
        copy.awaitingColorChoicePlayerId = this.awaitingColorChoicePlayerId;
        copy.awaitingColorChoicePermanentId = this.awaitingColorChoicePermanentId;
        copy.pendingColorChoiceETBTargetId = this.pendingColorChoiceETBTargetId;
        copy.colorChoiceContext = this.colorChoiceContext;
        copy.awaitingMayAbilityPlayerId = this.awaitingMayAbilityPlayerId;
        copy.awaitingMultiPermanentChoicePlayerId = this.awaitingMultiPermanentChoicePlayerId;
        copy.awaitingMultiPermanentChoiceValidIds = this.awaitingMultiPermanentChoiceValidIds != null ? new HashSet<>(this.awaitingMultiPermanentChoiceValidIds) : null;
        copy.awaitingMultiPermanentChoiceMaxCount = this.awaitingMultiPermanentChoiceMaxCount;
        copy.awaitingMultiGraveyardChoicePlayerId = this.awaitingMultiGraveyardChoicePlayerId;
        copy.awaitingMultiGraveyardChoiceValidCardIds = this.awaitingMultiGraveyardChoiceValidCardIds != null ? new HashSet<>(this.awaitingMultiGraveyardChoiceValidCardIds) : null;
        copy.awaitingMultiGraveyardChoiceMaxCount = this.awaitingMultiGraveyardChoiceMaxCount;
        copy.awaitingLibraryReorderPlayerId = this.awaitingLibraryReorderPlayerId;
        copy.awaitingLibraryReorderCards = this.awaitingLibraryReorderCards != null ? new ArrayList<>(this.awaitingLibraryReorderCards) : null;
        copy.awaitingLibraryReorderToBottom = this.awaitingLibraryReorderToBottom;
        copy.awaitingLibrarySearchPlayerId = this.awaitingLibrarySearchPlayerId;
        copy.awaitingLibrarySearchCards = this.awaitingLibrarySearchCards != null ? new ArrayList<>(this.awaitingLibrarySearchCards) : null;
        copy.awaitingLibrarySearchReveals = this.awaitingLibrarySearchReveals;
        copy.awaitingLibrarySearchCanFailToFind = this.awaitingLibrarySearchCanFailToFind;
        copy.awaitingLibrarySearchTargetPlayerId = this.awaitingLibrarySearchTargetPlayerId;
        copy.awaitingLibrarySearchRemainingCount = this.awaitingLibrarySearchRemainingCount;
        copy.awaitingLibrarySearchSourceCards = this.awaitingLibrarySearchSourceCards != null ? new ArrayList<>(this.awaitingLibrarySearchSourceCards) : null;
        copy.awaitingLibrarySearchReorderRemainingToBottom = this.awaitingLibrarySearchReorderRemainingToBottom;
        copy.awaitingLibrarySearchShuffleAfterSelection = this.awaitingLibrarySearchShuffleAfterSelection;
        copy.awaitingLibrarySearchPrompt = this.awaitingLibrarySearchPrompt;
        copy.awaitingLibrarySearchDestination = this.awaitingLibrarySearchDestination;
        copy.awaitingDiscardRemainingCount = this.awaitingDiscardRemainingCount;
        copy.awaitingRevealedHandChoiceTargetPlayerId = this.awaitingRevealedHandChoiceTargetPlayerId;
        copy.awaitingRevealedHandChoiceRemainingCount = this.awaitingRevealedHandChoiceRemainingCount;
        copy.awaitingRevealedHandChosenCards.addAll(this.awaitingRevealedHandChosenCards);
        copy.awaitingRevealedHandChoiceDiscardMode = this.awaitingRevealedHandChoiceDiscardMode;
        copy.awaitingHandTopBottomPlayerId = this.awaitingHandTopBottomPlayerId;
        copy.awaitingHandTopBottomCards = this.awaitingHandTopBottomCards != null ? new ArrayList<>(this.awaitingHandTopBottomCards) : null;
        copy.awaitingLibraryRevealPlayerId = this.awaitingLibraryRevealPlayerId;
        copy.awaitingLibraryRevealAllCards = this.awaitingLibraryRevealAllCards != null ? new ArrayList<>(this.awaitingLibraryRevealAllCards) : null;
        copy.awaitingLibraryRevealValidCardIds = this.awaitingLibraryRevealValidCardIds != null ? new HashSet<>(this.awaitingLibraryRevealValidCardIds) : null;
        copy.awaitingMultiZoneExileChoicePlayerId = this.awaitingMultiZoneExileChoicePlayerId;
        copy.awaitingMultiZoneExileChoiceValidCardIds = this.awaitingMultiZoneExileChoiceValidCardIds != null ? new HashSet<>(this.awaitingMultiZoneExileChoiceValidCardIds) : null;
        copy.awaitingMultiZoneExileChoiceMaxCount = this.awaitingMultiZoneExileChoiceMaxCount;
        return copy;
    }

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
        this.graveyardChoiceGainLifeEqualToManaValue = false;
        this.graveyardChoiceAttachToSourcePermanentId = null;
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

    public void setGraveyardChoiceGainLifeEqualToManaValue(boolean value) {
        this.graveyardChoiceGainLifeEqualToManaValue = value;
    }

    public boolean graveyardChoiceGainLifeEqualToManaValue() {
        return this.graveyardChoiceGainLifeEqualToManaValue;
    }

    public void setGraveyardChoiceAttachToSourcePermanentId(UUID permanentId) {
        this.graveyardChoiceAttachToSourcePermanentId = permanentId;
    }

    public UUID graveyardChoiceAttachToSourcePermanentId() {
        return this.graveyardChoiceAttachToSourcePermanentId;
    }

    public void setPendingEquipmentAttach(UUID equipmentPermanentId, UUID targetPermanentId) {
        this.pendingEquipmentAttachEquipmentId = equipmentPermanentId;
        this.pendingEquipmentAttachTargetId = targetPermanentId;
    }

    public UUID pendingEquipmentAttachEquipmentId() {
        return this.pendingEquipmentAttachEquipmentId;
    }

    public UUID pendingEquipmentAttachTargetId() {
        return this.pendingEquipmentAttachTargetId;
    }

    public void clearPendingEquipmentAttach() {
        this.pendingEquipmentAttachEquipmentId = null;
        this.pendingEquipmentAttachTargetId = null;
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

    public void beginLibrarySearch(LibrarySearchParams params) {
        this.awaitingInput = AwaitingInput.LIBRARY_SEARCH;
        this.awaitingLibrarySearchPlayerId = params.playerId();
        this.awaitingLibrarySearchCards = params.cards();
        this.awaitingLibrarySearchReveals = params.reveals();
        this.awaitingLibrarySearchCanFailToFind = params.canFailToFind();
        this.awaitingLibrarySearchTargetPlayerId = params.targetPlayerId();
        this.awaitingLibrarySearchRemainingCount = params.remainingCount();
        this.awaitingLibrarySearchSourceCards = params.sourceCards();
        this.awaitingLibrarySearchReorderRemainingToBottom = params.reorderRemainingToBottom();
        this.awaitingLibrarySearchShuffleAfterSelection = params.shuffleAfterSelection();
        this.awaitingLibrarySearchPrompt = params.prompt();
        this.awaitingLibrarySearchDestination = params.destination();
        this.context = new InteractionContext.LibrarySearch(params.playerId(), params.cards(), params.reveals(),
                params.canFailToFind(), params.targetPlayerId(), params.remainingCount(), params.sourceCards(),
                params.reorderRemainingToBottom(), params.shuffleAfterSelection(), params.prompt(), params.destination());
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
        this.awaitingLibrarySearchDestination = LibrarySearchDestination.HAND;
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

    public LibrarySearchDestination awaitingLibrarySearchDestination() {
        return this.awaitingLibrarySearchDestination;
    }

    public void beginLibraryRevealChoice(UUID playerId, List<Card> allCards, Set<UUID> validCardIds) {
        beginLibraryRevealChoice(playerId, allCards, validCardIds, false);
    }

    public void beginLibraryRevealChoice(UUID playerId, List<Card> allCards, Set<UUID> validCardIds, boolean remainingToGraveyard) {
        this.awaitingInput = AwaitingInput.LIBRARY_REVEAL_CHOICE;
        this.awaitingLibraryRevealPlayerId = playerId;
        this.awaitingLibraryRevealAllCards = allCards;
        this.awaitingLibraryRevealValidCardIds = validCardIds;
        this.context = new InteractionContext.LibraryRevealChoice(playerId, allCards, validCardIds, remainingToGraveyard);
    }

    public void beginLibraryRevealChoice(UUID playerId, List<Card> allCards, Set<UUID> validCardIds,
                                          boolean remainingToGraveyard, boolean selectedToHand,
                                          boolean reorderRemainingToBottom) {
        this.awaitingInput = AwaitingInput.LIBRARY_REVEAL_CHOICE;
        this.awaitingLibraryRevealPlayerId = playerId;
        this.awaitingLibraryRevealAllCards = allCards;
        this.awaitingLibraryRevealValidCardIds = validCardIds;
        this.context = new InteractionContext.LibraryRevealChoice(playerId, allCards, validCardIds,
                remainingToGraveyard, selectedToHand, reorderRemainingToBottom);
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
                awaitingLibrarySearchShuffleAfterSelection, awaitingLibrarySearchPrompt, awaitingLibrarySearchDestination);
    }

    public InteractionContext.LibraryRevealChoice libraryRevealChoiceContext() {
        if (context instanceof InteractionContext.LibraryRevealChoice lrc) return lrc;
        if (awaitingLibraryRevealPlayerId == null || awaitingLibraryRevealAllCards == null || awaitingLibraryRevealValidCardIds == null) return null;
        return new InteractionContext.LibraryRevealChoice(awaitingLibraryRevealPlayerId,
                awaitingLibraryRevealAllCards, awaitingLibraryRevealValidCardIds, false);
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

    public void beginMultiZoneExileChoice(UUID playerId, Set<UUID> validCardIds, int maxCount, UUID targetPlayerId, UUID controllerId, String cardName) {
        this.awaitingInput = AwaitingInput.MULTI_ZONE_EXILE_CHOICE;
        this.awaitingMultiZoneExileChoicePlayerId = playerId;
        this.awaitingMultiZoneExileChoiceValidCardIds = new HashSet<>(validCardIds);
        this.awaitingMultiZoneExileChoiceMaxCount = maxCount;
        this.context = new InteractionContext.MultiZoneExileChoice(playerId, new HashSet<>(validCardIds), maxCount, targetPlayerId, controllerId, cardName);
    }

    public void clearMultiZoneExileChoice() {
        this.awaitingMultiZoneExileChoicePlayerId = null;
        this.awaitingMultiZoneExileChoiceValidCardIds = null;
        this.awaitingMultiZoneExileChoiceMaxCount = 0;
    }

    public UUID awaitingMultiZoneExileChoicePlayerId() {
        return this.awaitingMultiZoneExileChoicePlayerId;
    }

    public Set<UUID> awaitingMultiZoneExileChoiceValidCardIds() {
        return this.awaitingMultiZoneExileChoiceValidCardIds;
    }

    public int awaitingMultiZoneExileChoiceMaxCount() {
        return this.awaitingMultiZoneExileChoiceMaxCount;
    }

    public InteractionContext.MultiZoneExileChoice multiZoneExileChoiceContext() {
        if (context instanceof InteractionContext.MultiZoneExileChoice mzec) return mzec;
        if (awaitingMultiZoneExileChoicePlayerId == null || awaitingMultiZoneExileChoiceValidCardIds == null) return null;
        return new InteractionContext.MultiZoneExileChoice(awaitingMultiZoneExileChoicePlayerId,
                awaitingMultiZoneExileChoiceValidCardIds, awaitingMultiZoneExileChoiceMaxCount, null, null, null);
    }

    public void beginCombatDamageAssignment(UUID playerId, int attackerIndex, UUID attackerPermanentId,
                                             String attackerName, int totalDamage,
                                             List<CombatDamageTarget> validTargets, boolean isTrample,
                                             boolean isDeathtouch) {
        this.awaitingInput = AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT;
        this.context = new InteractionContext.CombatDamageAssignment(playerId, attackerIndex, attackerPermanentId,
                attackerName, totalDamage, validTargets, isTrample, isDeathtouch);
    }

    public void clearCombatDamageAssignment() {
        // No extra fields to clear — context is cleared by clearAwaitingInput()
    }

    public InteractionContext.CombatDamageAssignment combatDamageAssignmentContext() {
        if (context instanceof InteractionContext.CombatDamageAssignment cda) return cda;
        return null;
    }

    public void beginXValueChoice(UUID playerId, int maxValue, String prompt, String cardName) {
        this.awaitingInput = AwaitingInput.X_VALUE_CHOICE;
        this.context = new InteractionContext.XValueChoice(playerId, maxValue, prompt, cardName);
    }

    public void beginKnowledgePoolCastChoice(UUID playerId, Set<UUID> validCardIds, int maxCount) {
        this.awaitingInput = AwaitingInput.KNOWLEDGE_POOL_CAST_CHOICE;
        this.awaitingMultiGraveyardChoicePlayerId = playerId;
        this.awaitingMultiGraveyardChoiceValidCardIds = new HashSet<>(validCardIds);
        this.awaitingMultiGraveyardChoiceMaxCount = maxCount;
        this.context = new InteractionContext.KnowledgePoolCastChoice(playerId, new HashSet<>(validCardIds), maxCount);
    }

    public void clearKnowledgePoolCastChoice() {
        this.awaitingMultiGraveyardChoicePlayerId = null;
        this.awaitingMultiGraveyardChoiceValidCardIds = null;
        this.awaitingMultiGraveyardChoiceMaxCount = 0;
    }

    public InteractionContext.KnowledgePoolCastChoice knowledgePoolCastChoiceContext() {
        if (context instanceof InteractionContext.KnowledgePoolCastChoice kpc) return kpc;
        if (awaitingMultiGraveyardChoicePlayerId == null || awaitingMultiGraveyardChoiceValidCardIds == null) return null;
        return new InteractionContext.KnowledgePoolCastChoice(awaitingMultiGraveyardChoicePlayerId,
                awaitingMultiGraveyardChoiceValidCardIds, awaitingMultiGraveyardChoiceMaxCount);
    }

    public InteractionContext.XValueChoice xValueChoiceContext() {
        if (context instanceof InteractionContext.XValueChoice xvc) return xvc;
        return null;
    }
}
