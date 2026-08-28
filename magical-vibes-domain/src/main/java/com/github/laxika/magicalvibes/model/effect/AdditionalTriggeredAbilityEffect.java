package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Static effect that makes a triggered ability from another matching permanent under the same
 * player's control trigger one additional time, optionally while a condition is met. The extended
 * constructor can also make the effect apply to the permanent carrying it and search all players'
 * battlefields, which is needed for effects that refer to a specific source and its attachments.
 */
public record AdditionalTriggeredAbilityEffect(PermanentPredicate sourcePredicate, Condition condition,
                                               boolean attackOnly, boolean includeSourcePermanent,
                                               boolean allControllers)
        implements CardEffect {

    public AdditionalTriggeredAbilityEffect(PermanentPredicate sourcePredicate) {
        this(sourcePredicate, null, false, false, false);
    }

    public AdditionalTriggeredAbilityEffect(PermanentPredicate sourcePredicate, Condition condition) {
        this(sourcePredicate, condition, false, false, false);
    }

    public AdditionalTriggeredAbilityEffect(PermanentPredicate sourcePredicate, Condition condition,
                                             boolean includeSourcePermanent, boolean allControllers) {
        this(sourcePredicate, condition, false, includeSourcePermanent, allControllers);
    }

    public static AdditionalTriggeredAbilityEffect forAttackTriggers(
            PermanentPredicate sourcePredicate, Condition condition) {
        return new AdditionalTriggeredAbilityEffect(sourcePredicate, condition, true, false, false);
    }
}
