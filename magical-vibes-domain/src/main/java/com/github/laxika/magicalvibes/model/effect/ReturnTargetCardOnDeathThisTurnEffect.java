package com.github.laxika.magicalvibes.model.effect;

/**
 * Registers a delayed trigger: when the targeted creature dies this turn, return that card to the
 * battlefield (tapped if {@code enterTapped}) under its owner's control.
 * <p>
 * At resolution, this effect reads the target permanent from the stack entry and records the
 * creature's card ID in {@code GameData.creaturesReturnedToBattlefieldOnDeathThisTurn}. When that
 * creature dies later in the same turn, the death pipeline pushes a triggered ability that returns
 * the card from its owner's graveyard to the battlefield. Used by Graceful Reprieve (untapped) and
 * Supernatural Stamina (tapped).
 */
public record ReturnTargetCardOnDeathThisTurnEffect(boolean enterTapped) implements CardEffect {

    /** Convenience for the untapped return (Graceful Reprieve). */
    public ReturnTargetCardOnDeathThisTurnEffect() {
        this(false);
    }
}
