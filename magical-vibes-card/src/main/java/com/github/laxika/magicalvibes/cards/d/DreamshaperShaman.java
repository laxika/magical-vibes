package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MayPayManaAndSacrificePermanentEffect;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandPermanentToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "THB", collectorNumber = "130")
public class DreamshaperShaman extends Card {

    public DreamshaperShaman() {
        addEffect(EffectSlot.CONTROLLER_END_STEP_TRIGGERED,
                new MayPayManaAndSacrificePermanentEffect(
                        "{2}{R}",
                        new PermanentNotPredicate(new PermanentIsLandPredicate()),
                        new RevealUntilNonlandPermanentToBattlefieldEffect(),
                        "nonland permanent"));
    }
}
