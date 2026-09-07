package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BolsterEffect;

@CardRegistration(set = "DTK", collectorNumber = "2")
public class AnafenzaKinTreeSpirit extends Card {

    public AnafenzaKinTreeSpirit() {
        // Whenever another nontoken creature you control enters, bolster 1.
        addEffect(EffectSlot.ON_ALLY_NONTOKEN_CREATURE_ENTERS_BATTLEFIELD, new BolsterEffect(1));
    }
}
