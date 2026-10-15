package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReadAheadEffect;

@CardRegistration(set = "WHO", collectorNumber = "14")
@CardRegistration(set = "WHO", collectorNumber = "335")
@CardRegistration(set = "WHO", collectorNumber = "619")
@CardRegistration(set = "WHO", collectorNumber = "926")
public class BarbaraWright extends Card {

    public BarbaraWright() {
        addEffect(EffectSlot.STATIC, new ReadAheadEffect());
    }
}
