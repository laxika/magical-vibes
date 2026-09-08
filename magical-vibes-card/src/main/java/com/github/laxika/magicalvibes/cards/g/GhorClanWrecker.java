package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RiotEffect;

@CardRegistration(set = "RNA", collectorNumber = "103")
public class GhorClanWrecker extends Card {

    public GhorClanWrecker() {
        addEffect(EffectSlot.STATIC, new RiotEffect());
    }
}
