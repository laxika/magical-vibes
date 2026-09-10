package com.github.laxika.magicalvibes.model.effect;

import java.util.UUID;

/**
 * Continuation for an amass instruction whose token creation may have paused for a replacement
 * choice. The target player and counter count are captured when the instruction begins.
 */
public record CompleteAmassGoblinsEffect(UUID playerId, int count, boolean drawCard) implements CardEffect {
}
