package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesOfChosenSubtypeEffect;

@CardRegistration(set = "ONS", collectorNumber = "23")
public class DefensiveManeuvers extends Card {

    public DefensiveManeuvers() {
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesOfChosenSubtypeEffect(0, 4));
    }
}
