package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.TriggeredAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

/**
 * Registers a delayed triggered ability until end of turn about the target creature (CR 603.7a, 603.7c): "whenever that
 * creature attacks one of your opponents this turn, you draw a card" (Maeve, Insidious Singer), "this turn, when target
 * creature you control attacks and isn't blocked, ..." (Delif's Cone), "whenever you attack this turn, Jaya deals
 * damage to that creature". The ability's trigger is the event it waits for; the creature is its non-targeting "that
 * creature", which it names as the entry's target unless the trigger is about the creature itself.
 *
 * @param ability    the delayed ability
 * @param targetSpec what the registering ability targets
 */
public record RegisterGlobalTriggeredAbilityAboutTargetUntilEndOfTurnEffect(TriggeredAbility ability,
                                                                          TargetSpec targetSpec)
        implements CardEffect {

    /** {@code ability} about a target creature. */
    public RegisterGlobalTriggeredAbilityAboutTargetUntilEndOfTurnEffect(TriggeredAbility ability) {
        this(ability, TargetSpec.benign(TargetPredicates.creature()));
    }

    /** {@code ability} about a target creature an opponent controls, which the registering ability harms. */
    public static RegisterGlobalTriggeredAbilityAboutTargetUntilEndOfTurnEffect aboutOpponentCreature(
            TriggeredAbility ability) {
        return new RegisterGlobalTriggeredAbilityAboutTargetUntilEndOfTurnEffect(ability,
                TargetSpec.harmful(TargetPredicates.creature(), new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())))));
    }
}
