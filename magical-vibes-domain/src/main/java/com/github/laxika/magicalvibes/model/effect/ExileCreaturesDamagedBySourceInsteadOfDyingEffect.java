package com.github.laxika.magicalvibes.model.effect;

/**
 * Static replacement effect: "If a creature dealt damage by this creature this turn would die, exile
 * it instead."
 *
 * <p>Only replaces a creature moving from the battlefield to a graveyard (dying), and only for
 * creatures recorded in {@code GameData.creatureCardsDamagedThisTurnBySourcePermanent} under this
 * permanent's id. Because the creature never reaches a graveyard, its dies-triggers do not fire.
 * Applied in {@code PermanentRemovalService}, which is the one place that knows the dying permanent
 * was a creature. On a creature this is "dealt damage by this creature"; on an Aura it is "dealt
 * damage by enchanted creature" (the Aura stays attached to the damaging permanent). The
 * replacement stops applying if the damaging permanent — or the Aura granting the ability — has
 * left the battlefield by the time the damaged creature dies. Used by Frostwielder and Kumano's
 * Blessing.
 */
public record ExileCreaturesDamagedBySourceInsteadOfDyingEffect() implements CardEffect {
}
