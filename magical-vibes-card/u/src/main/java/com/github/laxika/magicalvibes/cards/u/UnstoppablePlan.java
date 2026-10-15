package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "DFT", collectorNumber = "72")
public class UnstoppablePlan extends Card {

    public UnstoppablePlan() {
        // At the beginning of your end step, untap all nonland permanents you control.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new UntapPermanentsEffect(TapUntapScope.CONTROLLED,
                        new PermanentNotPredicate(new PermanentIsLandPredicate())));
    }
}
