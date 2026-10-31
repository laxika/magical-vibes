package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MustAttackEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "AVR", collectorNumber = "222")
public class TormentorsTrident extends Card {

    public TormentorsTrident() {
        addStaticEffect(new StaticBoostEffect(3, 0, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new MustAttackEffect());
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
