package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: "If a creature dealt damage by this creature this turn would die, exile
 * it instead."
 *
 * <p>Only replaces a creature moving from the battlefield to a graveyard (dying), and only for
 * creatures recorded in {@code GameData.creatureCardsDamagedThisTurnBySourcePermanent} under this
 * permanent's id. Because the creature never reaches a graveyard, its dies-triggers do not fire.
 * Applied in {@code PermanentRemovalService}, which is the one place that knows the dying permanent
 * was a creature. The replacement stops applying if the damaging permanent has left the battlefield
 * by the time the damaged creature dies. Used by Frostwielder.
 */
public record ExileCreaturesDamagedBySourceInsteadOfDyingEffect() implements CardEffect {
}
