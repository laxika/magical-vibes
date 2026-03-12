package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;

import java.util.UUID;

/**
 * Predicate that checks whether a state-triggered ability's condition is met
 * (MTG rule 603.8). Evaluated after state-based actions run.
 */
@FunctionalInterface
public interface StateTriggerPredicate {

    /**
     * @param gameData        the current game state
     * @param sourcePermanent the permanent that has the state trigger
     * @param controllerId    the controller of the permanent
     * @return {@code true} if the trigger condition is met
     */
    boolean test(GameData gameData, Permanent sourcePermanent, UUID controllerId);
}
