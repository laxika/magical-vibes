package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "AKH", collectorNumber = "71")
public class SlitherBlade extends Card {

    public SlitherBlade() {
        addEffect(EffectSlot.STATIC, new CantBeBlockedEffect());
    }
}
