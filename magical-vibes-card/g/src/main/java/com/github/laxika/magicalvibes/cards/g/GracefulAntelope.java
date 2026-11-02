package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.TargetLandBecomesBasicLandTypeUntilSourceLeavesEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ODY", collectorNumber = "24")
public class GracefulAntelope extends Card {

    public GracefulAntelope() {
        target(TargetFilters.land()).addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(
                        new TargetLandBecomesBasicLandTypeUntilSourceLeavesEffect(CardSubtype.PLAINS),
                        "Have target land become a Plains?"
                ));
    }
}
