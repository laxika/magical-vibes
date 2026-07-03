package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.interaction.LibrarySearchState;
import com.github.laxika.magicalvibes.model.interaction.LibraryViewState;
import com.github.laxika.magicalvibes.model.interaction.PermanentChoiceState;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class InteractionState {

    // --- Core state ---
    private AwaitingInput awaitingInput;
    private InteractionContext context;
    /** The currently active interaction for kinds migrated to the InteractionHandlerRegistry.
     *  While set, {@link #awaitingInput} still carries the legacy enum value (kept until the
     *  enum teardown stage) but {@link #context} stays null — the registry handler owns
     *  prompting, answer handling, and reconnect replay. */
    private PendingInteraction activeInteraction;

    // --- Grouped sub-states ---
    private PermanentChoiceState permanentChoice;
    private LibrarySearchState librarySearch;
    private final LibraryViewState libraryView = new LibraryViewState();

    // --- Independent fields (lifecycle not tied to a single begin/clear cycle) ---
    private PermanentChoiceContext permanentChoiceContext;
    private Card pendingAuraCard;
    private UUID pendingAuraOwnerId;
    private UUID pendingEquipmentAttachEquipmentId;
    private UUID pendingEquipmentAttachTargetId;

    /**
     * Creates a deep copy of this interaction state for AI simulation.
     */
    public InteractionState deepCopy() {
        InteractionState copy = new InteractionState();
        copy.awaitingInput = this.awaitingInput;
        copy.context = this.context;
        copy.activeInteraction = this.activeInteraction;
        copy.permanentChoice = this.permanentChoice != null ? this.permanentChoice.deepCopy() : null;
        copy.librarySearch = this.librarySearch != null ? this.librarySearch.deepCopy() : null;
        LibraryViewState lvCopy = this.libraryView.deepCopy();
        copy.libraryView.setReveal(lvCopy.revealPlayerId(), lvCopy.revealAllCards(), lvCopy.revealValidCardIds());
        copy.permanentChoiceContext = this.permanentChoiceContext;
        copy.pendingAuraCard = this.pendingAuraCard;
        copy.pendingAuraOwnerId = this.pendingAuraOwnerId;
        copy.pendingEquipmentAttachEquipmentId = this.pendingEquipmentAttachEquipmentId;
        copy.pendingEquipmentAttachTargetId = this.pendingEquipmentAttachTargetId;
        return copy;
    }

    // ========================================================================
    // Core awaiting input
    // ========================================================================

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

    /**
     * Marks the given registry-managed interaction as the currently active one. The legacy
     * {@link AwaitingInput} value is set alongside it so enum-based awaiting checks keep
     * working until the enum is removed.
     */
    public void beginInteraction(PendingInteraction interaction, AwaitingInput legacyType) {
        this.activeInteraction = interaction;
        this.awaitingInput = legacyType;
    }

    /** The active registry-managed interaction, or {@code null} when none (or a legacy kind) is active. */
    public PendingInteraction activeInteraction() {
        return this.activeInteraction;
    }

    /** The active interaction if it is of the given kind, or {@code null} otherwise. */
    public <T extends PendingInteraction> T activeInteraction(Class<T> type) {
        return type.isInstance(this.activeInteraction) ? type.cast(this.activeInteraction) : null;
    }

    public void clearAwaitingInput() {
        this.awaitingInput = null;
        this.context = null;
        this.activeInteraction = null;
    }

    // ========================================================================
    // Sub-state accessors
    // ========================================================================

    public PermanentChoiceState permanentChoice() {
        return permanentChoice;
    }
    public LibrarySearchState librarySearch() {
        return librarySearch;
    }

    public LibraryViewState libraryView() {
        return libraryView;
    }

    // ========================================================================
    // Combat
    // ========================================================================

    public void beginAttackerDeclaration(UUID activePlayerId) {
        this.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;
        this.context = new InteractionContext.AttackerDeclaration(activePlayerId);
    }

    public void beginBlockerDeclaration(UUID defenderId) {
        this.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;
        this.context = new InteractionContext.BlockerDeclaration(defenderId);
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

    // ========================================================================
    // Permanent choice
    // ========================================================================

    public void beginPermanentChoice(UUID playerId, Set<UUID> validIds, PermanentChoiceContext choiceContext) {
        this.awaitingInput = AwaitingInput.PERMANENT_CHOICE;
        this.permanentChoice = new PermanentChoiceState(playerId, new HashSet<>(validIds));
        this.permanentChoiceContext = choiceContext;
        this.context = new InteractionContext.PermanentChoice(playerId, new HashSet<>(validIds), choiceContext);
    }

    public void clearPermanentChoice() {
        this.permanentChoice = null;
        this.permanentChoiceContext = null;
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

    public InteractionContext.PermanentChoice permanentChoiceContextView() {
        if (context instanceof InteractionContext.PermanentChoice pc) return pc;
        if (permanentChoice == null) return null;
        return new InteractionContext.PermanentChoice(permanentChoice.playerId(),
                permanentChoice.validIds(), permanentChoiceContext);
    }

    // ========================================================================
    // Pending aura
    // ========================================================================

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

    public void setPendingAuraOwnerId(UUID ownerId) {
        this.pendingAuraOwnerId = ownerId;
    }

    public UUID consumePendingAuraOwnerId() {
        UUID ownerId = this.pendingAuraOwnerId;
        this.pendingAuraOwnerId = null;
        return ownerId;
    }

    // ========================================================================
    // Equipment attach
    // ========================================================================

    public void setPendingEquipmentAttach(UUID equipmentPermanentId, UUID targetId) {
        this.pendingEquipmentAttachEquipmentId = equipmentPermanentId;
        this.pendingEquipmentAttachTargetId = targetId;
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

    // ========================================================================
    // Library search
    // ========================================================================

    public void beginLibrarySearch(LibrarySearchParams params) {
        this.awaitingInput = AwaitingInput.LIBRARY_SEARCH;
        this.librarySearch = new LibrarySearchState(
                params.playerId(), params.cards(), params.reveals(), params.canFailToFind(),
                params.targetPlayerId(), params.remainingCount(), params.sourceCards(),
                params.reorderRemainingToBottom(), params.reorderRemainingToTop(),
                params.shuffleAfterSelection(),
                params.prompt(), params.destination(), params.filterCardTypes(),
                params.accumulatedCards(), params.filterCardName(), params.attachToPlayerId(),
                params.filterPredicate()
        );
        this.context = new InteractionContext.LibrarySearch(params.playerId(), params.cards(), params.reveals(),
                params.canFailToFind(), params.targetPlayerId(), params.remainingCount(), params.sourceCards(),
                params.reorderRemainingToBottom(), params.reorderRemainingToTop(),
                params.shuffleAfterSelection(), params.prompt(), params.destination(),
                params.filterCardTypes(), params.accumulatedCards(), params.filterCardName(),
                params.attachToPlayerId(), params.filterPredicate());
    }

    public void clearLibrarySearch() {
        this.librarySearch = null;
    }

    public InteractionContext.LibrarySearch librarySearchContext() {
        if (context instanceof InteractionContext.LibrarySearch ls) return ls;
        if (librarySearch == null) return null;
        return new InteractionContext.LibrarySearch(librarySearch.playerId(), librarySearch.cards(),
                librarySearch.reveals(), librarySearch.canFailToFind(),
                librarySearch.targetPlayerId(), librarySearch.remainingCount(),
                librarySearch.sourceCards(), librarySearch.reorderRemainingToBottom(),
                librarySearch.reorderRemainingToTop(),
                librarySearch.shuffleAfterSelection(), librarySearch.prompt(), librarySearch.destination(),
                librarySearch.filterCardTypes(), librarySearch.accumulatedCards(),
                librarySearch.filterCardName(), librarySearch.attachToPlayerId(),
                librarySearch.filterPredicate());
    }

    // ========================================================================
    // Library reveal choice
    // ========================================================================

    public void beginLibraryRevealChoice(UUID playerId, List<Card> allCards, Set<UUID> validCardIds) {
        beginLibraryRevealChoice(playerId, allCards, validCardIds, false);
    }

    public void beginLibraryRevealChoice(UUID playerId, List<Card> allCards, Set<UUID> validCardIds,
                                         boolean remainingToGraveyard) {
        this.awaitingInput = AwaitingInput.LIBRARY_REVEAL_CHOICE;
        this.libraryView.setReveal(playerId, allCards, validCardIds);
        this.context = new InteractionContext.LibraryRevealChoice(playerId, allCards, validCardIds, remainingToGraveyard);
    }

    public void beginLibraryRevealChoice(UUID playerId, List<Card> allCards, Set<UUID> validCardIds,
                                          boolean remainingToGraveyard, boolean selectedToHand,
                                          boolean reorderRemainingToBottom) {
        this.awaitingInput = AwaitingInput.LIBRARY_REVEAL_CHOICE;
        this.libraryView.setReveal(playerId, allCards, validCardIds);
        this.context = new InteractionContext.LibraryRevealChoice(playerId, allCards, validCardIds,
                remainingToGraveyard, selectedToHand, reorderRemainingToBottom);
    }

    public void beginLibraryRevealChoice(UUID playerId, List<Card> allCards, Set<UUID> validCardIds,
                                          boolean remainingToGraveyard, boolean selectedToHand,
                                          boolean reorderRemainingToBottom,
                                          int lifeCostPerSelection, UUID beneficiaryPlayerId) {
        this.awaitingInput = AwaitingInput.LIBRARY_REVEAL_CHOICE;
        this.libraryView.setReveal(playerId, allCards, validCardIds);
        this.context = new InteractionContext.LibraryRevealChoice(playerId, allCards, validCardIds,
                remainingToGraveyard, selectedToHand, reorderRemainingToBottom, false,
                lifeCostPerSelection, beneficiaryPlayerId);
    }

    public void beginLibraryRevealChoiceRandomBottom(UUID playerId, List<Card> allCards, Set<UUID> validCardIds) {
        this.awaitingInput = AwaitingInput.LIBRARY_REVEAL_CHOICE;
        this.libraryView.setReveal(playerId, allCards, validCardIds);
        this.context = new InteractionContext.LibraryRevealChoice(playerId, allCards, validCardIds,
                false, false, false, true, 0, null);
    }

    public void clearLibraryRevealChoice() {
        this.libraryView.clearReveal();
    }

    public InteractionContext.LibraryRevealChoice libraryRevealChoiceContext() {
        if (context instanceof InteractionContext.LibraryRevealChoice lrc) return lrc;
        if (libraryView.revealPlayerId() == null || libraryView.revealAllCards() == null
                || libraryView.revealValidCardIds() == null) return null;
        return new InteractionContext.LibraryRevealChoice(libraryView.revealPlayerId(),
                libraryView.revealAllCards(), libraryView.revealValidCardIds(), false);
    }

}
