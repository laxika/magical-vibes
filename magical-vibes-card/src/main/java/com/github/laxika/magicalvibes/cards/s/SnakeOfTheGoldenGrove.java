package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TributeEffect;
import com.github.laxika.magicalvibes.model.effect.TributeNotPaidEffect;

@CardRegistration(set = "BNG", collectorNumber = "141")
public class SnakeOfTheGoldenGrove extends Card {

    public SnakeOfTheGoldenGrove() {
        addEffect(EffectSlot.STATIC, new TributeEffect(3));
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new TributeNotPaidEffect(new GainLifeEffect(4)));
    }
}
