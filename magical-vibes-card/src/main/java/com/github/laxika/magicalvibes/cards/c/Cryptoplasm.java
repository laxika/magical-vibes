package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BecomeCopyOfTargetCreatureEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "MBS", collectorNumber = "23")
public class Cryptoplasm extends Card {

    public Cryptoplasm() {
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new BecomeCopyOfTargetCreatureEffect());
    }
}
