package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;

@CardRegistration(set = "5ED", collectorNumber = "34")
@CardRegistration(set = "ICE", collectorNumber = "31")
public class Hipparion extends Card {

    public Hipparion() {
        // This creature can't block creatures with power 3 or greater unless you pay {1}.
        addStaticEffect(CantAttackOrBlockUnlessPaysEffect.blocking(new Fixed(1), new PermanentPowerAtLeastPredicate(3)));
    }
}
