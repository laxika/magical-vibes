package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ApproachOfTheSecondSunEffect;

@CardRegistration(set = "AKH", collectorNumber = "4")
public class ApproachOfTheSecondSun extends Card {

    public ApproachOfTheSecondSun() {
        // If this spell was cast from your hand and you've cast another spell named Approach of the Second Sun
        // this game, you win the game. Otherwise, put Approach of the Second Sun into its owner's library
        // seventh from the top and you gain 7 life.
        addEffect(EffectSlot.SPELL, new ApproachOfTheSecondSunEffect());
    }
}
