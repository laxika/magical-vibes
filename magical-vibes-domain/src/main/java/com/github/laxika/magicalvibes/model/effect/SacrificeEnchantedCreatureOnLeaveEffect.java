package com.github.laxika.magicalvibes.model.effect;

/**
 * Leaves-the-battlefield trigger for reanimation auras (e.g. Animate Dead): "When this Aura leaves
 * the battlefield, that creature's controller sacrifices it."
 *
 * <p>"That creature" is the creature the Aura was attached to as it last existed on the battlefield (or, for an Aura
 * that could not attach, the reanimated creature it chose), read as the ability resolves from the stack entry's source
 * snapshot ({@code StackEntry.getSourcePermanentSnapshot()}).</p>
 */
public record SacrificeEnchantedCreatureOnLeaveEffect() implements CardEffect {
}
