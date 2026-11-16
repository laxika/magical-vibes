package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.trigger.AbilityTrigger;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

/**
 * Creates the delayed triggered ability "When you next activate an exhaust ability that isn't a mana ability this turn,
 * copy it. You may choose new targets for the copy." ({@link #delayedAbility}), which triggers only the next time it
 * applies and ends with the turn if it hasn't (CR 603.7b).
 */
public record CopyNextExhaustAbilityThisTurnEffect() implements CardEffect {

    /** The delayed triggered ability this creates. */
    public TriggeredAbility delayedAbility() {
        return new TriggeredAbility(Triggers.youActivateNonManaAbility().withKind(AbilityTrigger.AbilityKind.EXHAUST),
                new CopyTriggeringSpellOrAbilityEffect());
    }
}
