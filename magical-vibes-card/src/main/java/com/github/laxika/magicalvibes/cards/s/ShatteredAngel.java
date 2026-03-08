package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;

@CardRegistration(set = "NPH", collectorNumber = "23")
public class ShatteredAngel extends Card {

    public ShatteredAngel() {
        // Whenever a land an opponent controls enters, you may gain 3 life.
        addEffect(EffectSlot.ON_OPPONENT_LAND_ENTERS_BATTLEFIELD,
                new MayEffect(new GainLifeEffect(3), "Gain 3 life?"));
    }
}
