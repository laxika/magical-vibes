package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateModalDoubleFacedCardFromTopTwoEffect;

@CardRegistration(set = "MB2", collectorNumber = "290")
@CardRegistration(set = "MB2", collectorNumber = "526")
public class BlurryVisionary extends Card {

    public BlurryVisionary() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new CreateModalDoubleFacedCardFromTopTwoEffect());
    }
}
