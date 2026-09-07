package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "SNC", collectorNumber = "32")
public class SpeakeasyServer extends Card {

    public SpeakeasyServer() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(new PermanentCount(
                new PermanentIsCreaturePredicate(), CountScope.CONTROLLER, true)));
    }
}
