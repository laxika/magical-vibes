package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "ONE", collectorNumber = "111")
public class TestamentBearer extends Card {

    public TestamentBearer() {
        addEffect(EffectSlot.ON_DEATH, LookAtTopCardsEffect.chooseNToHandRestToGraveyard(3, 1));
    }
}
