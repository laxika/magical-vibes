package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;

@CardRegistration(set = "PTK", collectorNumber = "157")
public class WieldingTheGreenDragon extends Card {

    public WieldingTheGreenDragon() {
        addSpellEffect(BoostEffect.target(4, 4));
    }
}
