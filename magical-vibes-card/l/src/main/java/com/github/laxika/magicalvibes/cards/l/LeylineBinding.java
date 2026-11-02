package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTP", collectorNumber = "4")
@CardRegistration(set = "OTP", collectorNumber = "66")
@CardRegistration(set = "MAR", collectorNumber = "2")
@CardRegistration(set = "OMB", collectorNumber = "2")
@CardRegistration(set = "DMU", collectorNumber = "24")
public class LeylineBinding extends Card {

    public LeylineBinding() {
        addStaticEffect(new ReduceOwnCastCostEffect(
                new BasicLandTypesAmongControlledLands()));

        target(TargetFilters.nonlandPermanentAnOpponentControls())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new ExileTargetPermanentUntilSourceLeavesEffect());
    }
}
