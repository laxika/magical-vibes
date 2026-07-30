package com.github.laxika.magicalvibes.model.effect;

/**
 * Punisher removal aimed at everything that damaged the source this turn: for each creature that
 * dealt damage to the source permanent this turn, destroy that creature unless its controller pays
 * {@code lifeCost} life. A creature destroyed this way can't be regenerated.
 *
 * <p>Each qualifying creature is an independent decision made by that creature's controller, offered
 * one at a time through the may-ability system; a controller who can't pay has their creature
 * destroyed immediately. Used by Giant Albatross in the {@code ON_DEATH} slot, wrapped in a
 * {@link MayPayManaEffect} for the "you may pay {1}{U}" gate.
 *
 * @param lifeCost how much life a damaging creature's controller may pay to save it
 */
public record DestroyCreaturesThatDamagedSourceUnlessControllerPaysLifeEffect(int lifeCost) implements CardEffect {
}
