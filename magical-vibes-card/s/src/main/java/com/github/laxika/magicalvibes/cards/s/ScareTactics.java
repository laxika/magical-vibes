package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;

@CardRegistration(set = "EXO", collectorNumber = "73")
public class ScareTactics extends Card {

    public ScareTactics() {
        addSpellEffect(BoostEffect.creaturesYouControl(1, 0));
    }
}
