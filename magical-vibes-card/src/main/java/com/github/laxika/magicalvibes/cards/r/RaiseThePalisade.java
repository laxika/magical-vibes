package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCreaturesExceptChosenTypeEffect;

@CardRegistration(set = "SLD", collectorNumber = "1897")
public class RaiseThePalisade extends Card {

    public RaiseThePalisade() {
        addEffect(EffectSlot.SPELL, new ReturnAllCreaturesExceptChosenTypeEffect());
    }
}
