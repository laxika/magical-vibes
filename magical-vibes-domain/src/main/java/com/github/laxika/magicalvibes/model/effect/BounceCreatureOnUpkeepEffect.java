package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.TargetFilter;

import java.util.Set;

public record BounceCreatureOnUpkeepEffect(
        Scope scope,
        Set<TargetFilter> filters,
        String prompt
) implements CardEffect {

    public enum Scope {
        SOURCE_CONTROLLER,
        TRIGGER_TARGET_PLAYER
    }

    public BounceCreatureOnUpkeepEffect() {
        this(Scope.TRIGGER_TARGET_PLAYER, Set.of(), "Choose a creature to return to its owner's hand.");
    }
}
