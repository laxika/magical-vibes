package com.github.laxika.magicalvibes.model;

import java.util.UUID;

public class InteractionState {

    // --- Core state ---
    /** The currently active interaction — the registry handler owns prompting, answer
     *  handling, and reconnect replay. */
    private PendingInteraction activeInteraction;
    /** Stable identity shared by the initial decision delivery and every reconnect replay. */
    private UUID activeDecisionId;

    // --- Independent fields (lifecycle not tied to a single begin/clear cycle) ---
    private PermanentChoiceContext permanentChoiceContext;
    private Card pendingAuraCard;
    private Card pendingAuraOriginalCard;
    private UUID pendingAuraOwnerId;
    private StackEntry pendingAuraResolutionEntry;
    private UUID pendingEquipmentAttachEquipmentId;
    private UUID pendingEquipmentAttachTargetId;

    /**
     * Creates a deep copy of this interaction state for AI simulation.
     */
    public InteractionState deepCopy() {
        InteractionState copy = new InteractionState();
        copy.activeInteraction = this.activeInteraction;
        if (activeInteraction instanceof PendingInteraction.ColorChoice choice) {
            copy.activeInteraction = choice.copyCardTypeOnEnterPermanent();
        }
        if (activeInteraction instanceof PendingInteraction.DiscardChoice discard) {
            copy.activeInteraction = discard.deepCopy();
        }
        if (activeInteraction instanceof PendingInteraction.RevealAnyNumberOfCardsFromHandChoice reveal
                && (reveal.amplifyEntry() != null || reveal.duplicateManaValueRevealContext() != null)) {
            copy.activeInteraction = new PendingInteraction.RevealAnyNumberOfCardsFromHandChoice(
                    reveal.playerId(), reveal.validCardIds(), reveal.cardName(), reveal.manaAbilityContext(),
                    reveal.activatedAbilityContext(), reveal.eachPlayerRevealContext(),
                    reveal.amplifyEntry() == null ? null : reveal.amplifyEntry().deepCopy(),
                    reveal.duplicateManaValueRevealContext());
        }
        if (activeInteraction instanceof PendingInteraction.ColorChoice choice
                && choice.context() instanceof ChoiceContext.CardNameChoice nameChoice
                && nameChoice.preparedPermanent() != null) {
            ChoiceContext.CardNameChoice copiedNameChoice = new ChoiceContext.CardNameChoice(
                    nameChoice.card(), nameChoice.controllerId(), nameChoice.excludedTypes(),
                    nameChoice.nonbasicLandOnly(), nameChoice.attachedTo(), nameChoice.requiredType(),
                    nameChoice.landPlayZone(), new Permanent(nameChoice.preparedPermanent()));
            copy.activeInteraction = new PendingInteraction.ColorChoice(choice.playerId(), choice.permanentId(),
                    choice.etbTargetId(), copiedNameChoice, choice.options(), choice.prompt(), choice.disabledOptions());
        }
        copy.activeDecisionId = this.activeDecisionId;
        copy.permanentChoiceContext = copyPermanentChoiceContext(this.permanentChoiceContext);
        if (activeInteraction instanceof PendingInteraction.PermanentChoice choice) {
            PermanentChoiceContext context = choice.context() == this.permanentChoiceContext
                    ? copy.permanentChoiceContext : copyPermanentChoiceContext(choice.context());
            copy.activeInteraction = new PendingInteraction.PermanentChoice(choice.playerId(),
                    choice.validPermanentIds(), choice.validPlayerIds(), context, choice.prompt());
        }
        if (activeInteraction instanceof PendingInteraction.MultiPermanentChoice choice
                && choice.context() instanceof MultiPermanentChoiceContext.SacrificePermanentsToEnter sacrifice) {
            copy.activeInteraction = new PendingInteraction.MultiPermanentChoice(choice.playerId(), choice.validIds(),
                    choice.validPlayerIds(), choice.validCardIds(), choice.maxCount(),
                    new MultiPermanentChoiceContext.SacrificePermanentsToEnter(sacrifice.controllerId(),
                            new Permanent(sacrifice.enteringPermanent()), sacrifice.requiredCount()), choice.prompt());
        }
        copy.pendingAuraCard = this.pendingAuraCard;
        copy.pendingAuraOriginalCard = this.pendingAuraOriginalCard;
        copy.pendingAuraOwnerId = this.pendingAuraOwnerId;
        copy.pendingAuraResolutionEntry = this.pendingAuraResolutionEntry == null
                ? null : new StackEntry(this.pendingAuraResolutionEntry);
        copy.pendingEquipmentAttachEquipmentId = this.pendingEquipmentAttachEquipmentId;
        copy.pendingEquipmentAttachTargetId = this.pendingEquipmentAttachTargetId;
        return copy;
    }

    private static PermanentChoiceContext copyPermanentChoiceContext(PermanentChoiceContext context) {
        if (context instanceof PermanentChoiceContext.SpellTargetTriggerAnyTarget trigger) {
            return trigger.copyPlanarSnapshot();
        }
        if (context instanceof PermanentChoiceContext.FreeCastSacrificeCost cost) return cost.deepCopy();
        if (context instanceof PermanentChoiceContext.FreeCastBeholdCost cost) return cost.deepCopy();
        if (context instanceof PermanentChoiceContext.AuraEntryBatchChoice batch) return batch.deepCopy();
        if (context instanceof PermanentChoiceContext.PreparedTokenCopyAttachments copies) return copies.deepCopy();
        if (context instanceof PermanentChoiceContext.PreparedOpponentTokenCopiesAttacking copies) return copies.deepCopy();
        if (context instanceof PermanentChoiceContext.SacrificePermanentToEnter sacrifice) {
            return new PermanentChoiceContext.SacrificePermanentToEnter(sacrifice.controllerId(),
                    new Permanent(sacrifice.enteringPermanent()));
        }
        if (context instanceof PermanentChoiceContext.LandCasualty casualty) {
            return new PermanentChoiceContext.LandCasualty(casualty.controllerId(),
                    new Permanent(casualty.enteringPermanent()));
        }
        return context;
    }

    // ========================================================================
    // Core awaiting input
    // ========================================================================

    public boolean isAwaitingInput() {
        return this.activeInteraction != null;
    }

    /** Marks the given registry-managed interaction as the currently active one. */
    public void beginInteraction(PendingInteraction interaction) {
        beginInteraction(interaction, UUID.randomUUID());
    }

    /** Restores an interaction with an existing stable identity (used by simulation copies). */
    public void beginInteraction(PendingInteraction interaction, UUID decisionId) {
        this.activeInteraction = interaction;
        this.activeDecisionId = decisionId;
    }

    /**
     * Swaps the active interaction for an equivalent record, keeping {@link #activeDecisionId()}.
     * Only for presentation-only refinements made before the prompt is projected (the projector
     * reads the live record at dispatch time), never for changing what is being decided — the
     * decision identity must stay stable for reconnect replay and answer matching.
     */
    public void replaceActiveInteraction(PendingInteraction interaction) {
        if (this.activeInteraction == null) {
            throw new IllegalStateException("No active interaction to replace");
        }
        this.activeInteraction = interaction;
    }

    /** The active registry-managed interaction, or {@code null} when none is active. */
    public PendingInteraction activeInteraction() {
        return this.activeInteraction;
    }

    public UUID activeDecisionId() {
        return this.activeDecisionId;
    }

    /** The active interaction if it is of the given kind, or {@code null} otherwise. */
    public <T extends PendingInteraction> T activeInteraction(Class<T> type) {
        return type.isInstance(this.activeInteraction) ? type.cast(this.activeInteraction) : null;
    }

    public void clearAwaitingInput() {
        this.activeInteraction = null;
        this.activeDecisionId = null;
    }

    // ========================================================================
    // Permanent choice context (pre-seed carrier)
    // ========================================================================
    // The ~60 permanent-choice begin sites pre-seed this field with the operation to run when
    // the chosen permanent arrives; PlayerInputService.beginPermanentChoice/beginAnyTargetChoice
    // snapshot it into the active PendingInteraction.PermanentChoice record. It stays a separate
    // field (rather than folding into the record's begin arguments) because its lifecycle spans
    // interactions: e.g. the clone-copy may-choice pre-seeds it before the MAY_ABILITY_CHOICE
    // window, and a decline clears it without any permanent-choice begin ever happening.

    public void setPermanentChoiceContext(PermanentChoiceContext choiceContext) {
        this.permanentChoiceContext = choiceContext;
    }

    public PermanentChoiceContext permanentChoiceContext() {
        return this.permanentChoiceContext;
    }

    public void clearPermanentChoiceContext() {
        this.permanentChoiceContext = null;
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

    public void setPendingAuraOriginalCard(Card originalCard) {
        this.pendingAuraOriginalCard = originalCard;
    }

    public Card consumePendingAuraOriginalCard() {
        Card originalCard = this.pendingAuraOriginalCard;
        this.pendingAuraOriginalCard = null;
        return originalCard;
    }

    public void setPendingAuraOwnerId(UUID ownerId) {
        this.pendingAuraOwnerId = ownerId;
    }

    public UUID consumePendingAuraOwnerId() {
        UUID ownerId = this.pendingAuraOwnerId;
        this.pendingAuraOwnerId = null;
        return ownerId;
    }

    public void setPendingAuraResolutionEntry(StackEntry entry) {
        this.pendingAuraResolutionEntry = entry;
    }

    public StackEntry consumePendingAuraResolutionEntry() {
        StackEntry entry = this.pendingAuraResolutionEntry;
        this.pendingAuraResolutionEntry = null;
        return entry;
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

}
