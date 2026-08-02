package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostByBushidoEffect;
import com.github.laxika.magicalvibes.model.effect.BushidoEffect;

@CardRegistration(set = "CHK", collectorNumber = "46")
public class TakenoSamuraiGeneral extends Card {

    public TakenoSamuraiGeneral() {
        addEffect(EffectSlot.ON_BLOCK, new BushidoEffect(2));
        addEffect(EffectSlot.ON_BECOMES_BLOCKED, new BushidoEffect(2));
        addEffect(EffectSlot.STATIC, new BoostByBushidoEffect());
    }
}
