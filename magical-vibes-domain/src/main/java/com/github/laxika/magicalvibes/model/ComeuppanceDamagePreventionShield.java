package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CardEffect;
import java.util.UUID;

/** Turn-scoped Comeuppance prevention registered for one player's damage recipients. */
public record ComeuppanceDamagePreventionShield(UUID protectedPlayerId, Card sourceCard) implements CardEffect {
}
