package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.CardsInHand;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.Divided;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.MillHalfDefendingPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "SNC", collectorNumber = "197")
public class LordXanderTheCollector extends Card {

    public LordXanderTheCollector() {
        PermanentNotPredicate nonland = new PermanentNotPredicate(new PermanentIsLandPredicate());
        target(opponentTarget())
                .addTrigger(Triggers.enters(ObjectRelation.THIS),
                        new DiscardEffect(
                                new Divided(new CardsInHand(CountScope.TARGET_PLAYER), 2),
                                DiscardRecipient.TARGET_PLAYER))
                .addTrigger(Triggers.dies(ObjectRelation.THIS),
                        new SacrificePermanentsEffect(
                                new Divided(new PermanentCount(nonland, CountScope.TARGET_PLAYER), 2),
                                nonland,
                                SacrificeRecipient.TARGET_PLAYER));

        addTrigger(Triggers.attacks(), new MillHalfDefendingPlayerEffect(false));
    }

    private static PlayerPredicateTargetFilter opponentTarget() {
        return new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent");
    }
}
