package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentLosesLifeEffect;

@CardRegistration(set = "XLN", collectorNumber = "128")
public class ViciousConquistador extends Card {

    public ViciousConquistador() {
        addEffect(EffectSlot.ON_ATTACK, new EachOpponentLosesLifeEffect(1));
    }
}
