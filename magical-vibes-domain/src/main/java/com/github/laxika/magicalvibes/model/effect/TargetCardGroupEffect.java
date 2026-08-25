package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

/**
 * Associates an effect with the positional graveyard-card targets it resolves.
 * This is needed when one ability has several graveyard target groups with different effects.
 */
public interface TargetCardGroupEffect extends CardEffect {

    List<Integer> targetGroups();

    default boolean targetGroupsMustShareGraveyard() {
        return false;
    }
}
