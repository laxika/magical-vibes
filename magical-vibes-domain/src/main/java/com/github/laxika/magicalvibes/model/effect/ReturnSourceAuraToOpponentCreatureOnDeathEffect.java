package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentControlledByTriggeringPermanentControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

/**
 * Death trigger for auras: when the enchanted creature dies, its controller chooses target creature
 * one of their opponents controls, and this aura returns from its owner's graveyard to the
 * battlefield attached to that creature. Used by Necrotic Plague.
 *
 * <p>"Its controller" is the controller of the stack entry's triggering permanent, the creature that died, as it last
 * existed on the battlefield (CR 603.10a): that player chooses the target as the trigger is put on the stack
 * (CR 603.3d, {@link #targetChosenByTriggeringPermanentController()}), so the legal targets are the creatures that
 * player does not control. The trigger itself stays controlled by the aura's controller, and the chosen creature is
 * carried as the stack entry's target.</p>
 */
public record ReturnSourceAuraToOpponentCreatureOnDeathEffect() implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature(),
                new PermanentNotPredicate(new PermanentControlledByTriggeringPermanentControllerPredicate()));
    }

    @Override
    public boolean targetChosenByTriggeringPermanentController() {
        return true;
    }
}
