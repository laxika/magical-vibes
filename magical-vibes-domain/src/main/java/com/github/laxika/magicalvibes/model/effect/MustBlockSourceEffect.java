package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * When resolved, the targeted creature must block the source permanent this turn if able.
 * The sourcePermanentId is null in the card definition and gets snapshot at activation time.
 */
public record MustBlockSourceEffect(UUID sourcePermanentId) implements CardEffect {
    @Override public boolean canTargetPermanent() { return true; }
}
