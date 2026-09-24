package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DoubleMillForOpponentsEffect;

@CardRegistration(set = "SLD", collectorNumber = "2181")
@CardRegistration(set = "RVR", collectorNumber = "35")
@CardRegistration(set = "RVR", collectorNumber = "309")
public class BruvacTheGrandiloquent extends Card {

    public BruvacTheGrandiloquent() {
        addEffect(EffectSlot.STATIC, new DoubleMillForOpponentsEffect());
    }
}
