package com.github.laxika.magicalvibes.model.effect;

/**
 * STATIC-slot marker: whenever a creature its controller controls deals combat damage (to any
 * target), that controller gains that much life. Scanned inline during combat-damage processing in
 * {@code CombatDamageService} (there is no stack entry). Team-wide combat lifelink — unlike
 * {@link AllyCombatDamageTriggerEffect} it fires on combat damage to creatures and planeswalkers as
 * well as players, and grants life equal to the damage dealt. Used by Noble Purpose.
 */
public record GainLifeEqualToControlledCreatureCombatDamageEffect() implements CardEffect {
}
