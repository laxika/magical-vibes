package com.github.laxika.magicalvibes.model.effect;

/**
 * When resolved, registers a delayed triggered ability: "When this artifact leaves the battlefield this
 * turn, destroy that creature" (War Barge, the no-argument form), or with {@code alsoWhenSourceUntaps}
 * and no turn limit "When this creature leaves the battlefield or becomes untapped, destroy that
 * creature" (Merieke Ri Berit). Each resolution registers its own trigger for its own creature.
 *
 * <p>Reads the shared creature target and the ability's source permanent from the stack entry.
 */
public record RegisterDelayedDestroyTargetWhenSourceLeavesEffect(boolean alsoWhenSourceUntaps,
                                                                 boolean thisTurnOnly) implements CardEffect {

    public RegisterDelayedDestroyTargetWhenSourceLeavesEffect() {
        this(false, true);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.creature());
    }
}
