package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

import java.util.UUID;

/** Turn-scoped combat-damage prevention for a player with a token rider. */
public record CombatDamagePreventionTokenShield(
        UUID protectedPlayerId,
        CreateTokenEffect token,
        UUID tokenControllerId,
        String tokenSourceSetCode) {
}
