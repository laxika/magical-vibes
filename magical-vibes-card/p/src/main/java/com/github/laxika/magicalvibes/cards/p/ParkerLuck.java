package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetPlayersRevealTopCardsLoseLifeEqualToOtherManaValueThenToHandEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SPM", collectorNumber = "60")
@CardRegistration(set = "SPM", collectorNumber = "258")
@CardRegistration(set = "OM1", collectorNumber = "55")
public class ParkerLuck extends Card {

    public ParkerLuck() {
        PlayerPredicateTargetFilter playerTarget = new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        );
        target(playerTarget).addTrigger(Triggers.endStep(ActivePlayer.YOU),
                new TargetPlayersRevealTopCardsLoseLifeEqualToOtherManaValueThenToHandEffect());
        target(playerTarget);
    }
}
