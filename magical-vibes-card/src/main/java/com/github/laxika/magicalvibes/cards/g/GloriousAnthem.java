package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostOwnCreaturesEffect;

public class GloriousAnthem extends Card {

    public GloriousAnthem() {
        addEffect(EffectSlot.STATIC, new BoostOwnCreaturesEffect(1, 1));
    }
}
