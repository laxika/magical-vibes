package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantAllCreatureTypesToOwnCreaturesEffect;

@CardRegistration(set = "TSB", collectorNumber = "26")
public class MistformUltimus extends Card {

    public MistformUltimus() {
        addEffect(EffectSlot.STATIC, GrantAllCreatureTypesToOwnCreaturesEffect.toSelf());
    }
}
