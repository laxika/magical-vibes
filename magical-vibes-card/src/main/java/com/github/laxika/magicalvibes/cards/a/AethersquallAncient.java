package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.ControllerEnergyAtLeast;
import com.github.laxika.magicalvibes.model.effect.EnergyCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PayEnergyCost;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourceCardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

import java.util.List;

@CardRegistration(set = "KLD", collectorNumber = "39")
public class AethersquallAncient extends Card {

    public AethersquallAncient() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new EnergyCountersEffect(3));

        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(
                        new PayEnergyCost(8),
                        ReturnToHandEffect.allPermanentsMatching(new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourceCardPredicate())
                        )))
                ),
                "Pay eight {E}: Return all other creatures to their owners' hands. Activate only as a sorcery.",
                ActivationTimingRestriction.SORCERY_SPEED
        ).withActivationCondition(new ControllerEnergyAtLeast(8),
                "You need at least eight energy counters to activate this ability."));
    }
}
