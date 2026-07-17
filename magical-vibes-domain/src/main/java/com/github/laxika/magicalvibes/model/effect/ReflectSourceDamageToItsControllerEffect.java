package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardColor;

/**
 * Global-watcher marker for {@code ON_ANY_SOURCE_DEALS_DAMAGE}: whenever a source of the given
 * {@code color} (creature or spell) deals damage, the permanent holding this effect deals that
 * much damage to that source's controller (Justice). Never resolved directly — it is read by
 * {@code TriggerCollectionService.queueSourceDealsDamageReflections}, which queues a
 * {@code DealDamageToPlayersEffect(total, TARGET_PLAYER)} triggered ability at the source's
 * controller, with the watcher as the damage source.
 */
public record ReflectSourceDamageToItsControllerEffect(CardColor color) implements CardEffect {
}
