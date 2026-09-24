package com.github.laxika.magicalvibes.networking.model;

public record CombatDamageTargetView(String id, String name, int lethalDamageThreshold, int currentDamage, boolean isPlayer) {
}
