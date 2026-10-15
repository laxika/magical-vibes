package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.HalveCountersPutByOpponentsEffect;
import com.github.laxika.magicalvibes.model.effect.HalveTokenCreationEffect;

@CardRegistration(set = "MB2", collectorNumber = "309")
@CardRegistration(set = "MB2", collectorNumber = "545")
public class HalvingSeason extends Card {

    public HalvingSeason() {
        addEffect(EffectSlot.STATIC, new HalveTokenCreationEffect());
        addEffect(EffectSlot.STATIC, new HalveCountersPutByOpponentsEffect());
    }
}
