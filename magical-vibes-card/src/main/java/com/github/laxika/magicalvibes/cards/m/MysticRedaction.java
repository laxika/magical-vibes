package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "MH2", collectorNumber = "53")
public class MysticRedaction extends Card {

    public MysticRedaction() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new ScryEffect(1));
        addEffect(EffectSlot.ON_CONTROLLER_DISCARDS, new MillEffect(2, MillRecipient.EACH_OPPONENT));
    }
}
