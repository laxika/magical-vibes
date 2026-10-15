package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "AVR", collectorNumber = "120")
public class SoulcageFiend extends Card {

    public SoulcageFiend() {
        addEffect(EffectSlot.ON_DEATH, new LoseLifeEffect(3, LoseLifeRecipient.EACH_PLAYER));
    }
}
