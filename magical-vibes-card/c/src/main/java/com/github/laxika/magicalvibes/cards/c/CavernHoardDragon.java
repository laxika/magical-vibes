package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.GreatestPermanentCountAmongOpponents;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "HOC", collectorNumber = "23")
@CardRegistration(set = "HOC", collectorNumber = "63")
@CardRegistration(set = "LTC", collectorNumber = "31")
@CardRegistration(set = "LTC", collectorNumber = "114")
@CardRegistration(set = "LTC", collectorNumber = "433")
public class CavernHoardDragon extends Card {

    public CavernHoardDragon() {
        addStaticEffect(new ReduceOwnCastCostEffect(
                new GreatestPermanentCountAmongOpponents(new PermanentIsArtifactPredicate())));

        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                CreateTokenEffect.ofTreasureToken(new PermanentCount(
                        new PermanentIsArtifactPredicate(), CountScope.TARGET_PLAYER)));
    }
}
