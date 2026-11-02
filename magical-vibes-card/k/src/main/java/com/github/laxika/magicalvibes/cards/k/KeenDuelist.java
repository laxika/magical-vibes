package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetPlayersRevealTopCardsLoseLifeEqualToOtherManaValueThenToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SLD", collectorNumber = "828")
@CardRegistration(set = "SOC", collectorNumber = "218")
@CardRegistration(set = "C21", collectorNumber = "42")
public class KeenDuelist extends Card {

    public KeenDuelist() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.upkeep(ActivePlayer.YOU),
                TargetPlayersRevealTopCardsLoseLifeEqualToOtherManaValueThenToHandEffect
                        .forControllerAndTarget());
    }
}
