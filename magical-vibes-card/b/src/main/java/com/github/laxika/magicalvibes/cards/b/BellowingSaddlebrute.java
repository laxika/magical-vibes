package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.condition.Raid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;

@CardRegistration(set = "KTK", collectorNumber = "64")
public class BellowingSaddlebrute extends Card {

    public BellowingSaddlebrute() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, ConditionalEffect.unless(
                new NotCondition(new Raid()),
                new LoseLifeEffect(4)));
    }
}
