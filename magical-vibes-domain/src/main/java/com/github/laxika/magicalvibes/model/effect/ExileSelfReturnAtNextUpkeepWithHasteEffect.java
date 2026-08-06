package com.github.laxika.magicalvibes.model.effect;

/**
 * Exiles the source permanent and schedules it to return to the battlefield under its owner's
 * control at the beginning of its controller's next upkeep, gaining haste for as long as it stays
 * on the battlefield.
 *
 * <p>Obzedat, Ghost Council: "you may exile Obzedat. If you do, return it to the battlefield under
 * its owner's control at the beginning of your next upkeep. It gains haste." Wrap in a
 * {@link MayEffect} for the optional half. No-op when the source already left the battlefield.
 */
public record ExileSelfReturnAtNextUpkeepWithHasteEffect() implements CardEffect {
}
