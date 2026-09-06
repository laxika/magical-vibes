package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability interface for static effects that let a creature block through a normally
 * restrictive evasion ability. The combat legality service reads these facts from the creature,
 * granted effects, and attached Auras.
 */
public interface BlockabilityPermissionEffect extends CardEffect {

    /** Whether the carrier can block creatures with shadow as though it had shadow. */
    default boolean blocksShadowAsThoughShadow() {
        return false;
    }

    /** Whether the carrier can block creatures with landwalk as though they didn't have it. */
    default boolean blocksLandwalkAsThoughNoLandwalk() {
        return false;
    }
}
