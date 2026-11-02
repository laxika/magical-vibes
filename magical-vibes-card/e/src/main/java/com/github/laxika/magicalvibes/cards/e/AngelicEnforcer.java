package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.ControllerLifeTotal;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.GrantControllerKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SetLifeTotalEffect;
import com.github.laxika.magicalvibes.model.effect.SetPowerToughnessToAmountEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

public class AngelicEnforcer extends Card {

    public AngelicEnforcer() {
        ControllerLifeTotal life = new ControllerLifeTotal();
        addStaticEffect(new GrantControllerKeywordEffect(Keyword.HEXPROOF));
        addStaticEffect(new SetPowerToughnessToAmountEffect(life, life));
        addTrigger(Triggers.attacks(), new SetLifeTotalEffect(new Sum(life, life)));
    }
}
