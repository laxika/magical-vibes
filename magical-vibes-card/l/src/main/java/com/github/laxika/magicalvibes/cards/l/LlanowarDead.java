package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "APC", collectorNumber = "109")
public class LlanowarDead extends Card {

    public LlanowarDead() {
        addEffect(EffectSlot.ON_TAP, new AwardManaEffect(ManaColor.BLACK));
    }
}
