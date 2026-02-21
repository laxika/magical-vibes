package com.github.laxika.magicalvibes.model;

import java.util.UUID;

public record CombatDamageTarget(UUID id, String name, int effectiveToughness, int currentDamage, boolean isPlayer) {
}
