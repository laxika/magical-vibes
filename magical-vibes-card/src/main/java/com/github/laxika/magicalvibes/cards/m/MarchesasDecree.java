package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeMonarchEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;

@CardRegistration(set = "FDC", collectorNumber = "113")
public class MarchesasDecree extends Card {

    public MarchesasDecree() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new BecomeMonarchEffect());
        addEffect(EffectSlot.ON_CREATURE_ATTACKS_YOU,
                new LoseLifeEffect(1, LoseLifeRecipient.TARGET_PERMANENT_CONTROLLER));
    }
}
