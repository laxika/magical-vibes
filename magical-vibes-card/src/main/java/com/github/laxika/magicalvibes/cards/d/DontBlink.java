package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ShuffleCreaturesEnteringFromExileEffect;

@CardRegistration(set = "WHO", collectorNumber = "40")
public class DontBlink extends Card {

    public DontBlink() {
        addEffect(EffectSlot.SPELL, new ShuffleCreaturesEnteringFromExileEffect());
        addCycling("{2}");
    }
}
