package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * A player's permission to look at a face-down card in exile (CR 406.3) for as long as it remains exiled: a
 * floating effect on the exiled card ({@code FloatingContinuousEffect.onCard}) that ends as the card leaves exile
 * (CR 400.7). Kept apart from {@link ExilePlayPermissionEffect}: a card may be looked at without being playable.
 * A card may carry one for each player who may look at it.
 *
 * @param viewerId the player who may look at the card
 */
public record ExileLookPermissionEffect(UUID viewerId) implements CardEffect {
}
