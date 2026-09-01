package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "RAV", collectorNumber = "10")
public class ConclavePhalanx extends Card {

    public ConclavePhalanx() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new GainLifeEffect(
                new PermanentCount(new PermanentIsCreaturePredicate(), CountScope.CONTROLLER)));
    }
}
