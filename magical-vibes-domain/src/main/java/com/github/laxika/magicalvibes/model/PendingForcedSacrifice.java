package com.github.laxika.magicalvibes.model;

import java.util.List;
import java.util.UUID;

/**
 * Queued entry for forced sacrifice prompts when multiple players must choose
 * permanents to sacrifice sequentially (e.g. "Each player sacrifices five lands.").
 */
public record PendingForcedSacrifice(UUID playerId, int count, List<UUID> validPermanentIds) {
}
