package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostOwnCreaturesEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "10E", collectorNumber = "17")
public class GloriousAnthem extends Card {

    public GloriousAnthem() {
        addEffect(EffectSlot.STATIC, new BoostOwnCreaturesEffect(1, 1));
    }
}
