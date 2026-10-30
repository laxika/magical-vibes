package com.github.laxika.magicalvibes.model.effect;

/**
 * Destroys the creature blocking the source. {@code cannotBeRegenerated} makes this one destruction
 * ignore regeneration shields (Phyrexian Slayer), without affecting later destructions of that creature.
 */
public record DestroyCreatureBlockingThisEffect(boolean cannotBeRegenerated) implements CardEffect {

    public DestroyCreatureBlockingThisEffect() {
        this(false);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature());
    }
}
