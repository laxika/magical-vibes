package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "AVR", collectorNumber = "102")
public class ExquisiteBlood extends Card {

    public ExquisiteBlood() {
        // "Whenever an opponent loses life, you gain that much life." The life lost is snapshotted
        // onto the trigger's event value by the collector, read back here as the gain amount.
        addEffect(EffectSlot.ON_OPPONENT_LOSES_LIFE, new GainLifeEffect(new EventValue()));
    }
}
