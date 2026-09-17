package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "OTP", collectorNumber = "4")
@CardRegistration(set = "OTP", collectorNumber = "66")
@CardRegistration(set = "MAR", collectorNumber = "2")
@CardRegistration(set = "OMB", collectorNumber = "2")
@CardRegistration(set = "DMU", collectorNumber = "24")
public class LeylineBinding extends Card {

    public LeylineBinding() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(
                new BasicLandTypesAmongControlledLands()));

        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                        new ExileTargetPermanentUntilSourceLeavesEffect());
    }
}
