package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PlayersCantCycleCardsEffect;

@CardRegistration(set = "SCG", collectorNumber = "142")
public class Stabilizer extends Card {

    public Stabilizer() {
        addEffect(EffectSlot.STATIC, new PlayersCantCycleCardsEffect());
    }
}
