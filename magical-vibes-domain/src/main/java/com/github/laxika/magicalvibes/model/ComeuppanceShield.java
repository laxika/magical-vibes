package com.github.laxika.magicalvibes.model;

import java.util.UUID;

/**
 * Turn-scoped Comeuppance prevention for one player's life total and planeswalkers.
 *
 * @param protectedPlayerId the player and planeswalkers protected by the shield
 * @param sourceCard the Comeuppance card that deals the prevented damage back
 */
public record ComeuppanceShield(UUID protectedPlayerId, Card sourceCard) {
}
