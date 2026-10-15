package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SacrificeOnUnattachEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "5DN", collectorNumber = "126")
public class GraftedWargear extends Card {

    public GraftedWargear() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new SacrificeOnUnattachEffect());
        addActivatedAbility(new EquipActivatedAbility("{0}"));
    }
}
