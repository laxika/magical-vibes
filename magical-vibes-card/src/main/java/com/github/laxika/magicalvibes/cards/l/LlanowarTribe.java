package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "MH1", collectorNumber = "170")
public class LlanowarTribe extends Card {

    public LlanowarTribe() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.GREEN, 3));
    }
}
