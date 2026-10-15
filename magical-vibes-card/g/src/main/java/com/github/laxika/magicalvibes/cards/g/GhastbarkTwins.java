package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;

@CardRegistration(set = "M19", collectorNumber = "181")
public class GhastbarkTwins extends Card {

    public GhastbarkTwins() {
        addEffect(EffectSlot.STATIC, new GrantAdditionalBlockEffect(1));
    }
}
