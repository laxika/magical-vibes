package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessToAmountEffect;

@CardRegistration(set = "FIN", collectorNumber = "253")
@CardRegistration(set = "FIN", collectorNumber = "350")
public class AettirAndPriwen extends Card {

    public AettirAndPriwen() {
        ControllerLifeTotal life = new ControllerLifeTotal();
        addEffect(EffectSlot.STATIC, new SetBasePowerToughnessToAmountEffect(life, life,
                GrantScope.EQUIPPED_CREATURE));
        addActivatedAbility(new EquipActivatedAbility("{5}"));
    }
}
