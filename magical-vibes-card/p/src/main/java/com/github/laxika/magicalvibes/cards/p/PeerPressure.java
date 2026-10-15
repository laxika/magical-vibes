package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainControlOfCreaturesOfChosenTypeIfMoreThanEachOtherPlayerEffect;

@CardRegistration(set = "ONS", collectorNumber = "101")
public class PeerPressure extends Card {

    public PeerPressure() {
        addEffect(EffectSlot.SPELL, new GainControlOfCreaturesOfChosenTypeIfMoreThanEachOtherPlayerEffect());
    }
}
