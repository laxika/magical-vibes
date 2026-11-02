package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedPermanentCantBeTargetedByOpponentsEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "147")
@CardRegistration(set = "UMA", collectorNumber = "222")
@CardRegistration(set = "AFC", collectorNumber = "192")
public class ShieldingPlax extends Card {

    public ShieldingPlax() {
        target(TargetFilters.creature())
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect(1))
                .addStaticEffect(new EnchantedPermanentCantBeTargetedByOpponentsEffect());
    }
}
