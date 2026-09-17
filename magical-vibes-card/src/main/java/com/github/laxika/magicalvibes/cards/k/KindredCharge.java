package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfEachCreatureOfChosenTypeEffect;

@CardRegistration(set = "SPG", collectorNumber = "58")
public class KindredCharge extends Card {

    public KindredCharge() {
        addEffect(EffectSlot.SPELL, new CreateTokenCopyOfEachCreatureOfChosenTypeEffect());
    }
}
