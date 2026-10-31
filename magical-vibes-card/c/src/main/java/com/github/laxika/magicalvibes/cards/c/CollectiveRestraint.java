package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.BasicLandTypesAmongControlledLands;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;

@CardRegistration(set = "INV", collectorNumber = "49")
public class CollectiveRestraint extends Card {

    public CollectiveRestraint() {
        addStaticEffect(new RequirePaymentToAttackEffect(
                new BasicLandTypesAmongControlledLands(), false, null));
    }
}
