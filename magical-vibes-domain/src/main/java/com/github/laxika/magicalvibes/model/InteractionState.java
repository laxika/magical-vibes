package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.interaction.CardChoiceState;
import com.github.laxika.magicalvibes.model.interaction.ChoiceState;
import com.github.laxika.magicalvibes.model.interaction.GraveyardChoiceState;
import com.github.laxika.magicalvibes.model.interaction.LibrarySearchState;
import com.github.laxika.magicalvibes.model.interaction.LibraryViewState;
import com.github.laxika.magicalvibes.model.interaction.MultiSelectionState;
import com.github.laxika.magicalvibes.model.interaction.PermanentChoiceState;
import com.github.laxika.magicalvibes.model.interaction.RevealedHandChoiceState;

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
    private CardChoiceState cardChoice;
    private PermanentChoiceState permanentChoice;
    private GraveyardChoiceState graveyardChoice;
    private ChoiceState colorChoice;
    private LibrarySearchState librarySearch;
    private final LibraryViewState libraryView = new LibraryViewState();
    private RevealedHandChoiceState revealedHandChoice;
    private final MultiSelectionState multiSelection = new MultiSelectionState();

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
        copy.cardChoice = this.cardChoice != null ? this.cardChoice.deepCopy() : null;
        copy.permanentChoice = this.permanentChoice != null ? this.permanentChoice.deepCopy() : null;
        copy.graveyardChoice = this.graveyardChoice != null ? this.graveyardChoice.deepCopy() : null;
        copy.colorChoice = this.colorChoice != null ? this.colorChoice.deepCopy() : null;
        copy.librarySearch = this.librarySearch != null ? this.librarySearch.deepCopy() : null;
        LibraryViewState lvCopy = this.libraryView.deepCopy();
        copy.libraryView.setReveal(lvCopy.revealPlayerId(), lvCopy.revealAllCards(), lvCopy.revealValidCardIds());
        copy.revealedHandChoice = this.revealedHandChoice != null ? this.revealedHandChoice.deepCopy() : null;
        MultiSelectionState msCopy = this.multiSelection.deepCopy();
        copy.multiSelection.setMultiPermanent(msCopy.multiPermanentPlayerId(), msCopy.multiPermanentValidIds(), msCopy.multiPermanentMaxCount());
        copy.multiSelection.setMultiGraveyard(msCopy.multiGraveyardPlayerId(), msCopy.multiGraveyardValidCardIds(), msCopy.multiGraveyardMaxCount());
        copy.multiSelection.setMultiZoneExile(msCopy.multiZoneExilePlayerId(), msCopy.multiZoneExileValidCardIds(), msCopy.multiZoneExileMaxCount());
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

    public CardChoiceState cardChoice() {
        return cardChoice;
    }

    public PermanentChoiceState permanentChoice() {
        return permanentChoice;
    }

    public GraveyardChoiceState graveyardChoice() {
        return graveyardChoice;
    }

    public ChoiceState colorChoice() {
        return colorChoice;
    }

    public LibrarySearchState librarySearch() {
        return librarySearch;
    }

    public LibraryViewState libraryView() {
        return libraryView;
    }

    public RevealedHandChoiceState revealedHandChoice() {
        return revealedHandChoice;
    }

    public MultiSelectionState multiSelection() {
        return multiSelection;
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
    // Card choice
    // ========================================================================

    public void beginCardChoice(AwaitingInput type, UUID playerId, Set<Integer> validIndices, UUID targetId) {
        this.awaitingInput = type;
        this.cardChoice = new CardChoiceState(playerId, new HashSet<>(validIndices), targetId);
        this.context = new InteractionContext.CardChoice(type, playerId, new HashSet<>(validIndices), targetId);
    }

    public void clearCardChoice() {
        this.cardChoice = null;
    }

    public InteractionContext.CardChoice cardChoiceContext() {
        if (context instanceof InteractionContext.CardChoice cc) return cc;
        if (cardChoice == null || awaitingInput == null) return null;
        return new InteractionContext.CardChoice(awaitingInput, cardChoice.playerId(),
                cardChoice.validIndices(), cardChoice.targetId());
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
    // Graveyard choice
    // ========================================================================

    public void beginGraveyardChoice(UUID playerId, Set<Integer> validIndices,
                                     GraveyardChoiceDestination destination, List<Card> cardPool) {
        this.awaitingInput = AwaitingInput.GRAVEYARD_CHOICE;
        if (this.graveyardChoice == null) {
            this.graveyardChoice = new GraveyardChoiceState(playerId, new HashSet<>(validIndices), destination, cardPool);
        } else {
            // Preserve independently-set fields (gainLife, attachToSource, grantColor, grantSubtype, mayAbility)
            GraveyardChoiceState prev = this.graveyardChoice;
            this.graveyardChoice = new GraveyardChoiceState(playerId, new HashSet<>(validIndices), destination, cardPool);
            this.graveyardChoice.setGainLifeEqualToManaValue(prev.gainLifeEqualToManaValue());
            this.graveyardChoice.setAttachToSourcePermanentId(prev.attachToSourcePermanentId());
            this.graveyardChoice.setGrantColor(prev.grantColor());
            this.graveyardChoice.setGrantSubtype(prev.grantSubtype());
            this.graveyardChoice.setExileRemainingCount(prev.exileRemainingCount());
            this.graveyardChoice.setGainLifeIfCreatureAmount(prev.gainLifeIfCreatureAmount());
            this.graveyardChoice.setGainLifeIfCreaturePlayerId(prev.gainLifeIfCreaturePlayerId());
            if (prev.mayAbilitySourceCard() != null) {
                this.graveyardChoice.setMayAbilityContext(
                        prev.mayAbilitySourceCard(), prev.mayAbilityControllerId(),
                        prev.mayAbilityEffects(), prev.mayAbilitySourcePermanentId());
            }
        }
        this.context = new InteractionContext.GraveyardChoice(playerId, new HashSet<>(validIndices), destination, cardPool);
    }

    public void clearGraveyardChoice() {
        this.graveyardChoice = null;
    }

    public void prepareGraveyardChoice(GraveyardChoiceDestination destination, List<Card> cardPool) {
        ensureGraveyardChoice().setDestination(destination);
        ensureGraveyardChoice().setCardPool(cardPool);
    }

    public void setGraveyardChoiceGainLifeEqualToManaValue(boolean value) {
        ensureGraveyardChoice().setGainLifeEqualToManaValue(value);
    }

    public void setGraveyardChoiceAttachToSourcePermanentId(UUID permanentId) {
        ensureGraveyardChoice().setAttachToSourcePermanentId(permanentId);
    }

    public void setGraveyardChoiceGrantColor(CardColor color) {
        ensureGraveyardChoice().setGrantColor(color);
    }

    public void setGraveyardChoiceGrantSubtype(CardSubtype subtype) {
        ensureGraveyardChoice().setGrantSubtype(subtype);
    }

    public void setGraveyardChoiceExileRemainingCount(int count) {
        ensureGraveyardChoice().setExileRemainingCount(count);
    }

    public void setGraveyardChoiceGainLifeIfCreature(int amount, UUID playerId) {
        ensureGraveyardChoice().setGainLifeIfCreatureAmount(amount);
        ensureGraveyardChoice().setGainLifeIfCreaturePlayerId(playerId);
    }

    public void setGraveyardChoiceTrackWithSourcePermanentId(UUID permanentId) {
        ensureGraveyardChoice().setTrackWithSourcePermanentId(permanentId);
    }

    public InteractionContext.GraveyardChoice graveyardChoiceContext() {
        if (context instanceof InteractionContext.GraveyardChoice gc) return gc;
        if (graveyardChoice == null || graveyardChoice.playerId() == null) return null;
        return new InteractionContext.GraveyardChoice(graveyardChoice.playerId(),
                graveyardChoice.validIndices(), graveyardChoice.destination(), graveyardChoice.cardPool());
    }

    private GraveyardChoiceState ensureGraveyardChoice() {
        if (this.graveyardChoice == null) {
            this.graveyardChoice = new GraveyardChoiceState();
        }
        return this.graveyardChoice;
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
    // Color choice
    // ========================================================================

    public void beginColorChoice(UUID playerId, UUID permanentId, UUID etbTargetId,
                                 ChoiceContext choiceContext) {
        this.awaitingInput = AwaitingInput.COLOR_CHOICE;
        this.colorChoice = new ChoiceState(playerId, permanentId, etbTargetId, choiceContext);
        this.context = new InteractionContext.ColorChoice(playerId, permanentId, etbTargetId, choiceContext);
    }

    public void clearColorChoice() {
        this.colorChoice = null;
    }

    public ChoiceContext colorChoiceContext() {
        return colorChoice != null ? colorChoice.choiceContext() : null;
    }

    public InteractionContext.ColorChoice colorChoiceContextView() {
        if (context instanceof InteractionContext.ColorChoice cc) return cc;
        if (colorChoice == null) return null;
        return new InteractionContext.ColorChoice(colorChoice.playerId(), colorChoice.permanentId(),
                colorChoice.etbTargetId(), colorChoice.choiceContext());
    }

    // ========================================================================
    // Multi-permanent choice
    // ========================================================================

    public void beginMultiPermanentChoice(UUID playerId, Set<UUID> validIds, int maxCount) {
        this.awaitingInput = AwaitingInput.MULTI_PERMANENT_CHOICE;
        this.multiSelection.setMultiPermanent(playerId, new HashSet<>(validIds), maxCount);
        this.context = new InteractionContext.MultiPermanentChoice(playerId, new HashSet<>(validIds), maxCount);
    }

    public void clearMultiPermanentChoice() {
        this.multiSelection.clearMultiPermanent();
    }

    public InteractionContext.MultiPermanentChoice multiPermanentChoiceContext() {
        if (context instanceof InteractionContext.MultiPermanentChoice mpc) return mpc;
        if (multiSelection.multiPermanentPlayerId() == null || multiSelection.multiPermanentValidIds() == null) return null;
        return new InteractionContext.MultiPermanentChoice(multiSelection.multiPermanentPlayerId(),
                multiSelection.multiPermanentValidIds(), multiSelection.multiPermanentMaxCount());
    }

    // ========================================================================
    // Multi-graveyard choice
    // ========================================================================

    public void beginMultiGraveyardChoice(UUID playerId, Set<UUID> validCardIds, int maxCount) {
        this.awaitingInput = AwaitingInput.MULTI_GRAVEYARD_CHOICE;
        this.multiSelection.setMultiGraveyard(playerId, new HashSet<>(validCardIds), maxCount);
        this.context = new InteractionContext.MultiGraveyardChoice(playerId, new HashSet<>(validCardIds), maxCount);
    }

    public void clearMultiGraveyardChoice() {
        this.multiSelection.clearMultiGraveyard();
    }

    public InteractionContext.MultiGraveyardChoice multiGraveyardChoiceContext() {
        if (context instanceof InteractionContext.MultiGraveyardChoice mgc) return mgc;
        if (multiSelection.multiGraveyardPlayerId() == null || multiSelection.multiGraveyardValidCardIds() == null) return null;
        return new InteractionContext.MultiGraveyardChoice(multiSelection.multiGraveyardPlayerId(),
                multiSelection.multiGraveyardValidCardIds(), multiSelection.multiGraveyardMaxCount());
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

    // ========================================================================
    // Revealed hand choice
    // ========================================================================

    public void beginRevealedHandChoice(UUID choosingPlayerId, UUID targetPlayerId, Set<Integer> validIndices,
                                        int remainingCount, boolean discardMode, List<Card> chosenCards) {
        beginRevealedHandChoice(choosingPlayerId, targetPlayerId, validIndices, remainingCount, discardMode, false, chosenCards);
    }

    public void beginRevealedHandChoice(UUID choosingPlayerId, UUID targetPlayerId, Set<Integer> validIndices,
                                        int remainingCount, boolean discardMode, boolean exileMode, List<Card> chosenCards) {
        this.awaitingInput = AwaitingInput.REVEALED_HAND_CHOICE;
        this.revealedHandChoice = new RevealedHandChoiceState(
                choosingPlayerId, new HashSet<>(validIndices), targetPlayerId,
                remainingCount, discardMode, exileMode, chosenCards
        );
        // Also update cardChoice for backwards compatibility (shared fields)
        this.cardChoice = new CardChoiceState(choosingPlayerId, new HashSet<>(validIndices), null);
        this.context = new InteractionContext.RevealedHandChoice(
                choosingPlayerId, targetPlayerId, new HashSet<>(validIndices),
                remainingCount, discardMode, exileMode,
                new ArrayList<>(revealedHandChoice.chosenCardsSnapshot())
        );
    }

    public void beginRevealedHandChoiceFromCurrentState(UUID choosingPlayerId, UUID targetPlayerId,
                                                        Set<Integer> validIndices) {
        if (revealedHandChoice == null) return;
        List<Card> chosenCardsSnapshot = revealedHandChoice.chosenCardsSnapshot();
        beginRevealedHandChoice(
                choosingPlayerId, targetPlayerId, validIndices,
                revealedHandChoice.remainingCount(),
                revealedHandChoice.discardMode(),
                revealedHandChoice.exileMode(),
                chosenCardsSnapshot
        );
    }

    public void addRevealedHandChosenCard(Card card) {
        if (revealedHandChoice != null) {
            revealedHandChoice.addChosenCard(card);
        }
    }

    public int decrementRevealedHandChoiceRemainingCount() {
        return revealedHandChoice != null ? revealedHandChoice.decrementRemainingCount() : 0;
    }

    public void clearRevealedHandChoiceProgress() {
        if (revealedHandChoice != null) {
            revealedHandChoice.clearProgress();
        }
    }

    public InteractionContext.RevealedHandChoice revealedHandChoiceContext() {
        if (context instanceof InteractionContext.RevealedHandChoice rhc) return rhc;
        if (revealedHandChoice == null || cardChoice == null) return null;
        return new InteractionContext.RevealedHandChoice(
                revealedHandChoice.choosingPlayerId(), revealedHandChoice.targetPlayerId(),
                revealedHandChoice.validIndices(), revealedHandChoice.remainingCount(),
                revealedHandChoice.discardMode(), revealedHandChoice.exileMode(),
                revealedHandChoice.chosenCardsSnapshot());
    }

    // ========================================================================
    // Discard
    // ========================================================================

    public void setDiscardRemainingCount(int remainingCount) {
        ensureRevealedHandChoice().setDiscardRemainingCount(remainingCount);
    }

    public int decrementDiscardRemainingCount() {
        return revealedHandChoice != null ? revealedHandChoice.decrementDiscardRemainingCount() : 0;
    }

    private RevealedHandChoiceState ensureRevealedHandChoice() {
        if (this.revealedHandChoice == null) {
            this.revealedHandChoice = new RevealedHandChoiceState(null, null, null, 0, false, null);
        }
        return this.revealedHandChoice;
    }

    // ========================================================================
    // Multi-zone exile choice
    // ========================================================================

    public void beginMultiZoneExileChoice(UUID playerId, Set<UUID> validCardIds, int maxCount,
                                          UUID targetPlayerId, UUID controllerId, String cardName) {
        this.awaitingInput = AwaitingInput.MULTI_ZONE_EXILE_CHOICE;
        this.multiSelection.setMultiZoneExile(playerId, new HashSet<>(validCardIds), maxCount);
        this.context = new InteractionContext.MultiZoneExileChoice(playerId, new HashSet<>(validCardIds),
                maxCount, targetPlayerId, controllerId, cardName);
    }

    public void clearMultiZoneExileChoice() {
        this.multiSelection.clearMultiZoneExile();
    }

    public InteractionContext.MultiZoneExileChoice multiZoneExileChoiceContext() {
        if (context instanceof InteractionContext.MultiZoneExileChoice mzec) return mzec;
        if (multiSelection.multiZoneExilePlayerId() == null || multiSelection.multiZoneExileValidCardIds() == null) return null;
        return new InteractionContext.MultiZoneExileChoice(multiSelection.multiZoneExilePlayerId(),
                multiSelection.multiZoneExileValidCardIds(), multiSelection.multiZoneExileMaxCount(),
                null, null, null);
    }

}
