package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

@CardRegistration(set = "PCY", collectorNumber = "52")
public class TroublesomeSpirit extends Card {

    public TroublesomeSpirit() {
        // At the beginning of your end step, tap all lands you control.
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new TapPermanentsEffect(TapUntapScope.CONTROLLED, new PermanentIsLandPredicate()));
    }
}
