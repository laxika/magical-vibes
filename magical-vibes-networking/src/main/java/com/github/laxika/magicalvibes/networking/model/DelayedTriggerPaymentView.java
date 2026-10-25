package com.github.laxika.magicalvibes.networking.model;

import java.util.UUID;

/**
 * A delayed-trigger payment the viewer may make right now as a special action (CR 116.2c), such as
 * paying {U} so Quenchable Fire deals no additional damage at its caster's next upkeep.
 * {@code affectedId} is the player or planeswalker the delayed ability would affect; together with
 * {@code sourceCardId} it identifies the obligation in a {@code PAY_DELAYED_TRIGGER_COST} request.
 */
public record DelayedTriggerPaymentView(UUID sourceCardId, String sourceCardName, UUID affectedId,
                                        String affectedName, String manaCost) {
}
