package com.github.laxika.magicalvibes.model;

import java.util.List;
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

}
