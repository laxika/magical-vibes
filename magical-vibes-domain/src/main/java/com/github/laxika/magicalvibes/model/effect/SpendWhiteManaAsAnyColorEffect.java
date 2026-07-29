package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the controller may spend white mana as though it were mana of any color, and may
 * spend other mana only as though it were colorless mana (Celestial Dawn). Both halves are one
 * permission — the restriction on non-white mana is what makes the card a drawback as well as a
 * fixer, so they are modelled together.
 *
 * <p>Surfaced as a per-player flag on {@code ManaPool} (see
 * {@code ManaPool#isWhiteSpendableAsAnyColor}) that {@code ManaCost#canPay}/{@code pay} honor by
 * rewriting the pool before the ordinary payment path runs, set from
 * {@code GameQueryService.canSpendWhiteManaAsAnyColor(gameData, playerId)} at the
 * payment/affordability sites. Sibling of {@link SpendWhiteManaAsRedEffect}.
 */
public record SpendWhiteManaAsAnyColorEffect() implements CardEffect {
}
