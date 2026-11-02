package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.effect.HeistTargetOpponentLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCountersOnSelfEffect;
import com.github.laxika.magicalvibes.model.effect.SpellCastTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardControllerDoesNotOwnPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "YOTJ", collectorNumber = "8")
public class ThievingAven extends Card {

    public ThievingAven() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"))
                .addTrigger(Triggers.dealsCombatDamageToPlayer(),
                        new HeistTargetOpponentLibraryEffect());

        addTrigger(Triggers.youCastSpell(), new SpellCastTriggerEffect(
                new CardControllerDoesNotOwnPredicate(),
                List.of(new PutCountersOnSelfEffect(CounterType.PLUS_ONE_PLUS_ONE))));
    }
}
