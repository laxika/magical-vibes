package com.github.laxika.magicalvibes.model.amount;

/**
 * "The life you lost when it entered" (Soulgorger Orgg): the life the resolving ability's controller lost to the
 * triggering permanent's own enters ability, read from the stack entry's triggering permanent as it last existed on
 * the battlefield (CR 603.10a). 0 when the entry has no triggering permanent, when no life was recorded, or when the
 * life was lost by another player ("you" is the controller of the ability that reads it).
 */
public record LifeLostWhenTriggeringPermanentEntered() implements DynamicAmount {
}
