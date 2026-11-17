package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * A player's permission to cast an exiled creature card for as long as it remains exiled, while they control a
 * Lukka planeswalker (Lukka, Coppercoat Outcast): a floating effect on the exiled card
 * ({@code FloatingContinuousEffect.onCard}) that ends as the card leaves exile (CR 400.7).
 *
 * @param playerId the player who may cast the card
 */
public record LukkaExileCastPermissionEffect(UUID playerId) implements CardEffect {
}
