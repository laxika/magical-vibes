package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ForageEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSourceEffect;

@CardRegistration(set = "BLB", collectorNumber = "210")
public class CorpseberryCultivator extends Card {

    public CorpseberryCultivator() {
        addEffect(EffectSlot.BEGINNING_OF_COMBAT_TRIGGERED,
                new MayEffect(new ForageEffect(), "Forage?"));
        addEffect(EffectSlot.ON_CONTROLLER_FORAGES,
                new PutCountersOnSourceEffect(1, 1, 1));
    }
}
