package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CreaturesWithCounterAttackTogetherEffect;

@CardRegistration(set = "MIR", collectorNumber = "171")
public class EkunduCyclops extends Card {

    public EkunduCyclops() {
        addStaticEffect(CreaturesWithCounterAttackTogetherEffect.ifAnotherYouControlAttacks());
    }
}
