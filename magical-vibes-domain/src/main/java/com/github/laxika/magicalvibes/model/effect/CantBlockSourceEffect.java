package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * When resolved, the targeted creature can't block the source permanent this turn.
 * The sourcePermanentId is null in the card definition and gets snapshot at activation time.
 */
public record CantBlockSourceEffect(UUID sourcePermanentId) implements CardEffect {
}
