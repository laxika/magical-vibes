package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TormentOfHailfireEffect;

@CardRegistration(set = "HOU", collectorNumber = "77")
public class TormentOfHailfire extends Card {

    public TormentOfHailfire() {
        addEffect(EffectSlot.SPELL, new TormentOfHailfireEffect(3));
    }
}
