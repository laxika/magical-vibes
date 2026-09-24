package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnAllCreaturesExceptChosenTypeToHandEffect;

@CardRegistration(set = "MSC", collectorNumber = "278")
@CardRegistration(set = "MSC", collectorNumber = "340")
public class RaiseThePalisade extends Card {

    public RaiseThePalisade() {
        addEffect(EffectSlot.SPELL, new ReturnAllCreaturesExceptChosenTypeToHandEffect());
    }
}
