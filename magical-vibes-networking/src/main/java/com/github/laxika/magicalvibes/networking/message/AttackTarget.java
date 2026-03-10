package com.github.laxika.magicalvibes.networking.message;

/**
 * Describes a valid attack target: either the defending player or one of their planeswalkers.
 */
public record AttackTarget(String id, String name, boolean isPlayer) {
}
