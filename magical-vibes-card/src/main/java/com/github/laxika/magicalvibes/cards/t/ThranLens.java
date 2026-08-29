package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeColorlessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "ULG", collectorNumber = "133")
public class ThranLens extends Card {

    public ThranLens() {
        addEffect(EffectSlot.STATIC, new BecomeColorlessEffect(GrantScope.ALL_PERMANENTS));
    }
}
