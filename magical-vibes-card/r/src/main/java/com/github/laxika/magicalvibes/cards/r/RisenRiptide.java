package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.SetBasePowerToughnessEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZNR", collectorNumber = "73")
public class RisenRiptide extends Card {

    public RisenRiptide() {
        addTrigger(Triggers.youCastSpell().kicked(), new SetBasePowerToughnessEffect(5, 5, GrantScope.SELF));
    }
}
