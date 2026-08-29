package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;

@CardRegistration(set = "ULG", collectorNumber = "94")
public class ViashinoCutthroat extends Card {

    public ViashinoCutthroat() {
        addEffect(EffectSlot.END_STEP_TRIGGERED, ReturnToHandEffect.self());
    }
}
