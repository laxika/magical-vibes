package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;

@CardRegistration(set = "M14", collectorNumber = "116")
@CardRegistration(set = "ROE", collectorNumber = "126")
public class Shrivel extends Card {

    public Shrivel() {
        // All creatures get -1/-1 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-1, -1));
    }
}
