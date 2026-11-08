package com.github.laxika.magicalvibes.model.effect;

/**
 * Leaves-the-battlefield trigger for an Aura that destroys the creature it enchanted.
 *
 * <p>The enchanted creature is the one the Aura was attached to as it last existed on the battlefield, read as the
 * ability resolves from the stack entry's source snapshot ({@code StackEntry.getSourcePermanentSnapshot()}).</p>
 *
 * @param cannotBeRegenerated whether the destruction ignores regeneration shields
 */
public record DestroyEnchantedCreatureOnLeaveEffect(boolean cannotBeRegenerated) implements CardEffect {

    public DestroyEnchantedCreatureOnLeaveEffect() {
        this(true);
    }
}
