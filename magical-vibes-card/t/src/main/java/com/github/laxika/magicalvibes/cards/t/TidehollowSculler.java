package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.ChooseCardsFromTargetHandEffect;
import com.github.laxika.magicalvibes.model.effect.HandChoiceDestination;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ALA", collectorNumber = "202")
@CardRegistration(set = "MD1", collectorNumber = "2")
@CardRegistration(set = "MMA", collectorNumber = "184")
@CardRegistration(set = "TSR", collectorNumber = "388")
public class TidehollowSculler extends Card {

    public TidehollowSculler() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.OPPONENT),
                "Target must be an opponent"
        )).addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ChooseCardsFromTargetHandEffect(1, List.of(CardType.LAND), List.of(), HandChoiceDestination.EXILE, true)
                        .withReturnsOnSourceLeavesTrigger());
    }
}
