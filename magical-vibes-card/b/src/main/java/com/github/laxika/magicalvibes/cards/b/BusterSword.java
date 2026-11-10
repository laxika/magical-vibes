package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FIN", collectorNumber = "255")
@CardRegistration(set = "FIN", collectorNumber = "351")
public class BusterSword extends Card {

    public BusterSword() {
        addStaticEffect(new StaticBoostEffect(3, 2, GrantScope.EQUIPPED_CREATURE));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect(1),
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(new EventValue()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
