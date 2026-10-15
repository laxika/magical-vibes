package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "OTJ", collectorNumber = "23")
public class OutlawMedic extends Card {

    public OutlawMedic() {
        addEffect(EffectSlot.ON_DEATH, new DrawCardEffect());
    }
}
