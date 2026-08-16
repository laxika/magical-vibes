package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;

@CardRegistration(set = "M21", collectorNumber = "134")
public class BurnBright extends Card {

    public BurnBright() {
        addEffect(EffectSlot.SPELL, new BoostAllOwnCreaturesEffect(2, 0));
    }
}
