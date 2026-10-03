package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** Turn-scoped Judgment of Alexander prevention for one player's damage recipient. */
public record JudgmentOfAlexanderDamagePreventionShield(UUID protectedPlayerId, Card sourceCard) {
}
