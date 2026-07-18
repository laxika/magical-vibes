package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the controller may spend white mana as though it were red mana (Sunglasses of Urza).
 * A permission only — white mana keeps its actual color but may additionally pay red mana costs.
 * Surfaced as a per-player flag on {@code ManaPool} (see {@code ManaPool#isWhiteSpendableAsRed}) that
 * {@code ManaCost#canPay}/{@code pay} honor, set from
 * {@code GameQueryService.canSpendWhiteManaAsRed(gameData, playerId)} at the payment/affordability sites.
 */
public record SpendWhiteManaAsRedEffect() implements CardEffect {
}
