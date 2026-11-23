package com.github.laxika.magicalvibes.model.effect;

/**
 * Static effect: the controller's life total can't change.
 * (They can't gain or lose life. They can't pay any amount of life except 0.)
 * Damage is still dealt but doesn't cause the life total to change.
 * On a permanent it is a static ability (Platinum Emperion); floated on a player by
 * {@link GrantStaticEffectToPlayerEffect#untilYourNextTurn} it is "until your next turn, your life total can't change"
 * (Teferi's Protection, Perch Protection).
 */
public record LifeTotalCantChangeEffect() implements CardEffect {
}
