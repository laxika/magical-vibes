package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "10E", collectorNumber = "171")
@CardRegistration(set = "GVL", collectorNumber = "37")
@CardRegistration(set = "DDD", collectorNumber = "37")
@CardRegistration(set = "DDJ", collectorNumber = "49")
@CardRegistration(set = "DPA", collectorNumber = "31")
@CardRegistration(set = "M13", collectorNumber = "106")
@CardRegistration(set = "UDS", collectorNumber = "68")
@CardRegistration(set = "INV", collectorNumber = "120")
@CardRegistration(set = "8ED", collectorNumber = "158")
@CardRegistration(set = "9ED", collectorNumber = "157")
@CardRegistration(set = "P02", collectorNumber = "87")
@CardRegistration(set = "S99", collectorNumber = "86")
public class RavenousRats extends Card {

    public RavenousRats() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS), new DiscardEffect(1, DiscardRecipient.TARGET_PLAYER));
    }
}
