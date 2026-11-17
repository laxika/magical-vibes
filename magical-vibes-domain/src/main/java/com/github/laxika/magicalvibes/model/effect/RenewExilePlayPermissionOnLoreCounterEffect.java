package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * "During any turn you put a lore counter on this Saga, you may play that card" (Summon: Brynhildr): a floating
 * effect on the exiled card ({@code FloatingContinuousEffect.onCard}) that lasts for as long as the card remains
 * exiled (CR 400.7). Each time a lore counter is put on the Saga, the card gets an {@link ExilePlayPermissionEffect}
 * until end of turn.
 *
 * @param sagaPermanentId the Saga whose lore counters renew the permission
 */
public record RenewExilePlayPermissionOnLoreCounterEffect(UUID sagaPermanentId) implements CardEffect {
}
