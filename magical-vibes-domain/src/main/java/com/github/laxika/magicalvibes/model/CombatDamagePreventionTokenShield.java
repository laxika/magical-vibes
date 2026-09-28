package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

/** Turn-scoped combat-damage prevention shield with a token rider. */
public record CombatDamagePreventionTokenShield(CreateTokenEffect token, String sourceSetCode) {
}
