package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * One or more players exile the top {@code count} cards of their library, tracked as "exiled with"
 * the source permanent. Which players is set by {@code player} — you (Colfenor's Plans, Duplicity,
 * Search the City), target player or opponent (Mindreaver, Grimoire Thief), the player a combat-damage
 * or draw trigger was about (Nightveil Specter, Ian Malcolm), the defending player (Robber of the Rich),
 * or every player (Knowledge Pool). Pair with {@link AllowCastFromCardsExiledWithSourceEffect} to let a
 * player play those cards.
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
 * <p>A player subject that declares a target ({@link Subjects#targetPlayer()}) makes this a harmful
 * player target; the card's target filter narrows it to an opponent where the Oracle text says so.
 */
public record ExileTopCardsToSourceEffect(int count, boolean faceDown,
                                          boolean toGraveyardOnControlLoss, PlayerSubject player,
                                          DynamicAmount dynamicCount,
                                          boolean markWithIntelCounters, boolean mayLookAtFaceDownCards,
                                          boolean exilerMayLookWhileExiled)
        implements CardEffect {

    public ExileTopCardsToSourceEffect(int count, boolean faceDown,
                                       boolean toGraveyardOnControlLoss, PlayerSubject player,
                                       DynamicAmount dynamicCount, boolean markWithIntelCounters,
                                       boolean mayLookAtFaceDownCards) {
        this(count, faceDown, toGraveyardOnControlLoss, player, dynamicCount, markWithIntelCounters,
                mayLookAtFaceDownCards, false);
    }

    public ExileTopCardsToSourceEffect(int count, boolean faceDown,
                                       boolean toGraveyardOnControlLoss, PlayerSubject player,
                                       DynamicAmount dynamicCount, boolean markWithIntelCounters) {
        this(count, faceDown, toGraveyardOnControlLoss, player, dynamicCount, markWithIntelCounters, true);
    }

    public ExileTopCardsToSourceEffect(int count, boolean faceDown,
                                       boolean toGraveyardOnControlLoss, PlayerSubject player,
                                       DynamicAmount dynamicCount) {
        this(count, faceDown, toGraveyardOnControlLoss, player, dynamicCount, false);
    }

    /**
     * Face-down exile from the controller's own library where only the player who exiled the
     * card may look at it, for as long as it remains exiled, even if the source changes controller
     * or leaves the battlefield (Intet, the Dreamer).
     */
    public static ExileTopCardsToSourceEffect faceDownLookedAtByExiler(int count) {
        return new ExileTopCardsToSourceEffect(count, true, false, Subjects.you(),
                null, false, false, true);
    }

    /** Exiles cards face down without granting either player permission to look at them. */
    public static ExileTopCardsToSourceEffect faceDownWithoutLookPermission(int count) {
        return new ExileTopCardsToSourceEffect(count, true, false, Subjects.you(), null, false, false);
    }

    /** Face-down exile from the controller's own library (Colfenor's Plans). */
    public ExileTopCardsToSourceEffect(int count) {
        this(count, true, false, Subjects.you(), null);
    }

    public ExileTopCardsToSourceEffect(int count, boolean faceDown) {
        this(count, faceDown, false, Subjects.you(), null);
    }

    public ExileTopCardsToSourceEffect(int count, boolean faceDown, boolean toGraveyardOnControlLoss) {
        this(count, faceDown, toGraveyardOnControlLoss, Subjects.you(), null);
    }

    public ExileTopCardsToSourceEffect(int count, boolean faceDown, boolean toGraveyardOnControlLoss,
                                       PlayerSubject player) {
        this(count, faceDown, toGraveyardOnControlLoss, player, null);
    }

    public ExileTopCardsToSourceEffect(DynamicAmount dynamicCount, boolean faceDown,
                                       boolean toGraveyardOnControlLoss, PlayerSubject player) {
        this(dynamicCount, faceDown, toGraveyardOnControlLoss, player, false);
    }

    /** Combat-damage variant that marks each newly exiled card with an intel counter. */
    public ExileTopCardsToSourceEffect(DynamicAmount dynamicCount, boolean faceDown,
                                       boolean toGraveyardOnControlLoss, PlayerSubject player,
                                       boolean markWithIntelCounters) {
        this(0, faceDown, toGraveyardOnControlLoss, player, dynamicCount, markWithIntelCounters);
    }

    @Override
    public TargetSpec targetSpec() {
        return player.declaresTarget() ? TargetSpec.harmful(TargetPredicates.player()) : TargetSpec.NONE;
    }
}
