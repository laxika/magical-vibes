package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.OpponentsMustAttackControllerEffect;

@CardRegistration(set = "40K", collectorNumber = "85")
public class SeekerOfSlaanesh extends Card {

    public SeekerOfSlaanesh() {
        addStaticEffect(OpponentsMustAttackControllerEffect.attackingAnyone());
    }
}
