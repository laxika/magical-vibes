package com.github.laxika.magicalvibes.model.effect;

/**
 * "Each player may put any number of land cards from their hand onto the battlefield."
 * (The Great Aurora)
 *
 * <p>Every player gets the choice, in APNAP order, through
 * {@link com.github.laxika.magicalvibes.model.PendingInteraction.PutLandsFromHandChoice}: each
 * chooser picks any subset of the land cards in their hand and they all enter untapped at once.
 *
 * <p>Distinct from {@link PutCardToBattlefieldEffect#tappedAnyNumber} (Wrenn and Seven), which is a
 * repeated one-at-a-time choice for the controller only.
 */
public record EachPlayerMayPutLandsFromHandToBattlefieldEffect() implements CardEffect {
}
