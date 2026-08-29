package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasCountersPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "86")
public class TetzimocPrimalDeath extends Card {

    public TetzimocPrimalDeath() {
        addHandActivatedAbility(new ActivatedAbility(
                false,
                "{B}",
                List.of(new PutCounterOnTargetPermanentEffect(CounterType.PREY)),
                "{B}, Reveal Tetzimoc, Primal Death from your hand: Put a prey counter on target creature. Activate only during your turn.",
                TargetFilters.creature(), null, null, ActivationTimingRestriction.ONLY_DURING_YOUR_TURN
        ).withRevealsSourceFromHand());

        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DestroyAllPermanentsEffect(
                new PermanentAllOfPredicate(List.of(
                        new PermanentIsCreaturePredicate(),
                        new PermanentHasCountersPredicate(CounterType.PREY),
                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                ))));
    }
}
