package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "DOM", collectorNumber = "101")
@CardRegistration(set = "SLD", collectorNumber = "36")
@CardRegistration(set = "SPG", collectorNumber = "56")
@CardRegistration(set = "MB2", collectorNumber = "47")
public class RatColony extends Card {

    public RatColony() {
        // Rat Colony gets +1/+0 for each other Rat you control.
        addStaticEffect(new BoostSelfEffect(
                new PermanentCount(new PermanentHasSubtypePredicate(CardSubtype.RAT),
                        CountScope.CONTROLLER, true),
                new Fixed(0)));
    }
}
