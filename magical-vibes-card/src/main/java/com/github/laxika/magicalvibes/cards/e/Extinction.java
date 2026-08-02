package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesOfChosenTypeEffect;

@CardRegistration(set = "TMP", collectorNumber = "135")
public class Extinction extends Card {

    public Extinction() {
        addEffect(EffectSlot.SPELL, new DestroyAllCreaturesOfChosenTypeEffect());
    }
}
