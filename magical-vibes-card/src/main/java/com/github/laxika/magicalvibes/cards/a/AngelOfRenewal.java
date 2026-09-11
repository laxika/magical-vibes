package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "BFZ", collectorNumber = "18")
public class AngelOfRenewal extends Card {

    public AngelOfRenewal() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)));
    }
}
