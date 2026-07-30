package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "AVR", collectorNumber = "23")
public class GoldnightRedeemer extends Card {

    public GoldnightRedeemer() {
        // When this creature enters, you gain 2 life for each other creature you control.
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(new Scaled(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER, true), 2)));
    }
}
