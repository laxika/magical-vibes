package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import java.util.UUID;

/** Turn-scoped Judgment of Alexander prevention for one player's damage recipient. */
public record JudgmentOfAlexanderDamagePreventionShield(UUID protectedPlayerId, Card sourceCard) implements CardEffect {
}
