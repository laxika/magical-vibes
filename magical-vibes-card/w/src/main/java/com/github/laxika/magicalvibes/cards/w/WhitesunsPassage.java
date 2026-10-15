package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "SOM", collectorNumber = "27")
public class WhitesunsPassage extends Card {

    public WhitesunsPassage() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(5));
    }
}
