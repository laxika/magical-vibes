package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockEachCombatEffect;

@CardRegistration(set = "AFR", collectorNumber = "247")
@CardRegistration(set = "HBG", collectorNumber = "258")
public class IronGolem extends Card {

    public IronGolem() {
        addStaticEffect(new MustAttackEffect());
        addStaticEffect(new MustBlockEachCombatEffect());
    }
}
