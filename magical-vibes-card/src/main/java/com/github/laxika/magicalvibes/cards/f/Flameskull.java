package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceAndTopCardChooseOneMayPlayUntilNextTurnEffect;

@CardRegistration(set = "AFR", collectorNumber = "143")
public class Flameskull extends Card {

    public Flameskull() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
        addEffect(EffectSlot.ON_DEATH, new ExileSourceAndTopCardChooseOneMayPlayUntilNextTurnEffect());
    }
}
