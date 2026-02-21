package com.github.laxika.magicalvibes.networking.message;

import java.util.Map;

public record CombatDamageAssignedRequest(int attackerIndex, Map<String, Integer> damageAssignments) {
}
