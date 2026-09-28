package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.TargetPower;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ReduceActivationCostEffect;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;

@CardRegistration(set = "HBG", collectorNumber = "201")
public class BeltOfGiantStrength extends Card {

    public BeltOfGiantStrength() {
        addEffect(EffectSlot.STATIC,
                new SetBasePowerToughnessEffect(10, 10, GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{10}",
                new ReduceActivationCostEffect(new TargetPower())));
    }
}
