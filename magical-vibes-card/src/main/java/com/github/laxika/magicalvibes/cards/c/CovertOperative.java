package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;

@CardRegistration(set = "LGN", collectorNumber = "33")
public class CovertOperative extends Card {

    public CovertOperative() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
