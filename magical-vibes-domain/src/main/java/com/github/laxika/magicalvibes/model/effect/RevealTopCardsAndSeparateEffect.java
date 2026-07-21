package com.github.laxika.magicalvibes.model.effect;

/**
 * "Reveal the top {@code count} cards of your library. An opponent separates those cards into two
 * piles. Put one pile into your hand and the other into your graveyard." (Fact or Fiction; the
 * enters trigger on Unesh, Criosphinx Sovereign.)
 *
 * <p>Non-targeting. Resolution removes the top {@code count} cards from the controller's library,
 * hands the pile split to an opponent, then lets the controller choose which pile goes to their
 * hand (the other goes to their graveyard). Reuses the shared card-pile flow
 * ({@code PendingPileSeparation} with {@code CardPileDisposition.HAND}).
 */
public record RevealTopCardsAndSeparateEffect(int count) implements CardEffect {
}
