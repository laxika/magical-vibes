package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;

public class AngelicEnforcer extends Card {

    public AngelicEnforcer() {
        ControllerLifeTotal life = new ControllerLifeTotal();
        addEffect(EffectSlot.STATIC, new SetPowerToughnessToAmountEffect(life, life));
        addEffect(EffectSlot.ON_ATTACK, new SetLifeTotalEffect(new Sum(life, life)));
    }
}
