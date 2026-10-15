package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DoubleExploreReplacementEffect;

@CardRegistration(set = "LCC", collectorNumber = "63")
@CardRegistration(set = "LCC", collectorNumber = "95")
public class TopographyTracker extends Card {

    public TopographyTracker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, CreateTokenEffect.ofMapToken(1));
        addEffect(EffectSlot.STATIC, new DoubleExploreReplacementEffect());
    }
}
