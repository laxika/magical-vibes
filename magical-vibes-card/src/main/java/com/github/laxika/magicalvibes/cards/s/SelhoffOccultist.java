package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillTargetPlayerEffect;

@CardRegistration(set = "ISD", collectorNumber = "73")
public class SelhoffOccultist extends Card {

    public SelhoffOccultist() {
        // Whenever Selhoff Occultist or another creature dies, target player mills a card.
        addEffect(EffectSlot.ON_DEATH, new MillTargetPlayerEffect(1));
        addEffect(EffectSlot.ON_ANY_CREATURE_DIES, new MillTargetPlayerEffect(1));
    }
}
