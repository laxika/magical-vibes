package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfThenEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "ORI", collectorNumber = "241")
public class ThrowingKnife extends Card {

    public ThrowingKnife() {
        // Equipped creature gets +2/+0.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature attacks, you may sacrifice this Equipment.
        // If you do, this Equipment deals 2 damage to any target.
        addEffect(EffectSlot.ON_ATTACK,
                new MayEffect(new SacrificeSelfThenEffect(new DealDamageToAnyTargetEffect(2)),
                        "You may sacrifice Throwing Knife. If you do, it deals 2 damage to any target."));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
