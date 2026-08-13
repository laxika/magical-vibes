package com.github.laxika.magicalvibes.model.effect;

/**
 * One or more players exile the top {@code count} cards of their library, tracked as "exiled with"
 * the source permanent. Which players is set by {@code scope} — the controller alone (Colfenor's
 * Plans, Duplicity, Search the City), the chosen player (Mindreaver), a single opponent (Grimoire
 * Thief, Nightveil Specter), or every player (Knowledge Pool). Pair with
 * {@link AllowCastFromCardsExiledWithSourceEffect} to let a player play those cards.
 *
 * <p>The effect fizzles if the source permanent has left the battlefield by the time it resolves,
 * since there would be nothing to track the exiled cards with.
 *
 * <p>{@code faceDown} controls CR 406.3 visibility: Colfenor's Plans and Grimoire Thief exile face
 * down, while Search the City and Nightveil Specter exile face up (Search the City's own trigger
 * asks players to compare names against those cards).
 *
 * <p>{@code toGraveyardOnControlLoss} registers the source permanent for the control-loss watch
 * ({@code GameData.exiledCardsToGraveyardOnControlLossWatch}), so "when you lose control of this
 * permanent, put all cards exiled with it into their owner's graveyard" is honoured when control
 * changes or it leaves the battlefield. Used by Duplicity; see
 * {@link ExileCardFromHandFaceDownWithSourceEffect} for the same flag on the hand-exile variant.
 *
 * <p>When {@code targetedOpponent} is true, {@code scope} uses the chosen player target instead of
 * the derived opponent context used by combat-damage and two-player triggered abilities.
 */
public record ExileTopCardsToSourceEffect(int count, boolean faceDown,
                                          boolean toGraveyardOnControlLoss, LibraryScope scope,
                                          boolean targetedOpponent)
        implements CombatDamageTriggerContextEffect {

    /** Face-down exile from the controller's own library (Colfenor's Plans). */
    public ExileTopCardsToSourceEffect(int count) {
        this(count, true, false, LibraryScope.CONTROLLER, false);
    }

    public ExileTopCardsToSourceEffect(int count, boolean faceDown) {
        this(count, faceDown, false, LibraryScope.CONTROLLER, false);
    }

    public ExileTopCardsToSourceEffect(int count, boolean faceDown, boolean toGraveyardOnControlLoss) {
        this(count, faceDown, toGraveyardOnControlLoss, LibraryScope.CONTROLLER, false);
    }

    public ExileTopCardsToSourceEffect(int count, boolean faceDown, boolean toGraveyardOnControlLoss,
                                       LibraryScope scope) {
        this(count, faceDown, toGraveyardOnControlLoss, scope, false);
    }

    @Override
    public TargetSpec targetSpec() {
        return targetedOpponent || scope == LibraryScope.TARGET_PLAYER
                ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }

    /**
     * Only the opponent scope needs a combat-damage context: the exiling player is the damaged
     * player and the exiled cards must be tracked with the damage-dealing permanent, so both are
     * bound on the entry (Nightveil Specter). The other scopes derive their players without the
     * entry's {@code targetId}, so they take the plain stack entry.
     */
    @Override
    public TriggerContext combatDamageTriggerContext() {
        return scope == LibraryScope.TARGET_OPPONENT && !targetedOpponent
                ? TriggerContext.DAMAGED_PLAYER : null;
    }
}
