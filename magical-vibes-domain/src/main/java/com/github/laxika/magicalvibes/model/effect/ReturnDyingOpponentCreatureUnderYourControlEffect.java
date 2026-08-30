package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.UUID;

/**
 * Triggered effect: return a creature card that an opponent controlled — and that died with a
 * -1/-1 counter on it — from its owner's graveyard to the battlefield under your control.
 * Used by Necroskitter's death trigger.
 *
 * <p>The no-arg constructor ({@code dyingCardId} null) is used in the card definition; the
 * collector {@code DeathTriggerCollectorService#handleOpponentCreatureReturnUnderControl} stamps
 * the concrete dying card id when the trigger fires. Resolution steals it from its owner's
 * graveyard and tracks it as a stolen creature so it stays under the new controller's control.
 * Fizzles if it is no longer in a graveyard.
 *
 * @param dyingCardId the card ID of the dying creature (null in the card definition)
 * @param grantSubtype subtype granted to the returned permanent if it is a creature
 */
public record ReturnDyingOpponentCreatureUnderYourControlEffect(
        UUID dyingCardId,
        CardSubtype grantSubtype
) implements CardEffect {

    public ReturnDyingOpponentCreatureUnderYourControlEffect() {
        this(null, null);
    }

    public ReturnDyingOpponentCreatureUnderYourControlEffect(UUID dyingCardId) {
        this(dyingCardId, null);
    }

    public ReturnDyingOpponentCreatureUnderYourControlEffect(CardSubtype grantSubtype) {
        this(null, grantSubtype);
    }
}
