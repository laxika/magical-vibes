package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "KTK", collectorNumber = "73")
@CardRegistration(set = "SLD", collectorNumber = "969")
@CardRegistration(set = "SLD", collectorNumber = "2243")
public class GrimHaruspex extends Card {

    public GrimHaruspex() {
        addMorph("{B}");
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_DIES, new DrawCardEffect(1));
    }
}
