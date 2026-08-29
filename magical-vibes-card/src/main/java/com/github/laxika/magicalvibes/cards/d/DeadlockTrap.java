package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.EntersTappedEffect;
import com.github.laxika.magicalvibes.model.effect.LockTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TargetPredicates;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsPlaneswalkerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "204")
public class DeadlockTrap extends Card {

    public DeadlockTrap() {
        addEffect(EffectSlot.STATIC, new EntersTappedEffect());
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new EnergyCountersEffect(2));

        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new PayEnergyCost(1),
                        new TapPermanentsEffect(TapUntapScope.TARGET),
                        new LockTargetPermanentEffect(false, false, true,
                                EffectDuration.UNTIL_END_OF_TURN, TargetPredicates.creatureOrPlaneswalker())
                ),
                "{T}, Pay {E}: Tap target creature or planeswalker. Its activated abilities can't be activated this turn.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsPlaneswalkerPredicate()
                        )),
                        "Target must be a creature or planeswalker"
                )
        ).withActivationCondition(new ControllerEnergyAtLeast(1),
                "You need at least one energy counter to activate this ability."));
    }
}
