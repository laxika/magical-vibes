package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;

@CardRegistration(set = "PC2", collectorNumber = "104")
public class ShardlessAgent extends Card {

    public ShardlessAgent() {
        addEffect(EffectSlot.ON_SELF_CAST, new CascadeEffect());
    }
}
