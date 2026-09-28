package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.NoRegretsEgretEffect;

@CardRegistration(set = "MB2", collectorNumber = "297")
@CardRegistration(set = "MB2", collectorNumber = "533")
public class NoRegretsEgret extends Card {

    public NoRegretsEgret() {
        addEffect(EffectSlot.MULLIGAN_ACTION, new NoRegretsEgretEffect());
    }
}
