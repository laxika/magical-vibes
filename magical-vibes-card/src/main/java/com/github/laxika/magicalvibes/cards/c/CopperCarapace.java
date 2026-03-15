package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;

@CardRegistration(set = "MBS", collectorNumber = "102")
public class CopperCarapace extends Card {

    public CopperCarapace() {
        // Equipped creature gets +2/+2
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));

        // Equipped creature can't block
        addEffect(EffectSlot.STATIC, new CantBlockEffect());

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
