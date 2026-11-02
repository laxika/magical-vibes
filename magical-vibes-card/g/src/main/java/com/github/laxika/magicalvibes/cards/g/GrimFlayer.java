package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Delirium;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EMN", collectorNumber = "184")
@CardRegistration(set = "SIR", collectorNumber = "234")
@CardRegistration(set = "2X2", collectorNumber = "222")
@CardRegistration(set = "DSC", collectorNumber = "218")
public class GrimFlayer extends Card {

    public GrimFlayer() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new SurveilEffect(3));
        addStaticEffect(new ConditionalEffect(
                new Delirium(), new StaticBoostEffect(2, 2, GrantScope.SELF)));
    }
}
