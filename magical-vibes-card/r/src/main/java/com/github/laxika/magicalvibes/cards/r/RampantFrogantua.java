package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.amount.PlayersWhoLostGame;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.DynamicStaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerAndMayPutMilledLandsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M3C", collectorNumber = "66")
@CardRegistration(set = "M3C", collectorNumber = "118")
public class RampantFrogantua extends Card {

    public RampantFrogantua() {
        addStaticEffect(new DynamicStaticBoostEffect(
                new Scaled(new PlayersWhoLostGame(), 10),
                new Scaled(new PlayersWhoLostGame(), 10),
                GrantScope.SELF));
        addTrigger(Triggers.dealsCombatDamageToPlayer(), new MayEffect(
                new MillControllerAndMayPutMilledLandsOntoBattlefieldEffect(new EventValue()),
                "Mill that many cards?"));
    }
}
