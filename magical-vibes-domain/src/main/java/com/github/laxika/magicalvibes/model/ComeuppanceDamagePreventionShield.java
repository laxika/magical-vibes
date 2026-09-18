package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/** Turn-scoped Comeuppance prevention registered for one player's damage recipients. */
public record ComeuppanceDamagePreventionShield(UUID protectedPlayerId, Card sourceCard) {
}
