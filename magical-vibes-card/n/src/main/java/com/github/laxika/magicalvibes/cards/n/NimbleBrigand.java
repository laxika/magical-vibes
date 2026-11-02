package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.CommittedCrimeThisTurn;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTJ", collectorNumber = "58")
public class NimbleBrigand extends Card {

    public NimbleBrigand() {
        addStaticEffect(new ConditionalEffect(
                new CommittedCrimeThisTurn(),
                new GrantEffectEffect(new CantBeBlockedEffect(), GrantScope.SELF)));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new DrawCardEffect());
    }
}
