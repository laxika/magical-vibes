package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AllCreaturesAreTokensEffect;

@CardRegistration(set = "MB2", collectorNumber = "280")
@CardRegistration(set = "MB2", collectorNumber = "516")
public class IntangibleVibes extends Card {

    public IntangibleVibes() {
        addEffect(EffectSlot.STATIC, new AllCreaturesAreTokensEffect());
    }
}
