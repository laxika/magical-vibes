package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.ReturnChosenPermanentsToHandEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SOK", collectorNumber = "89")
public class SinkIntoTakenuma extends Card {

    public SinkIntoTakenuma() {
        target(new PlayerPredicateTargetFilter(
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "Target must be a player"
        )).addSpellEffect(ReturnChosenPermanentsToHandEffect.anyNumber(
                        Subjects.controlledBy(Subjects.you(), new PermanentHasSubtypePredicate(CardSubtype.SWAMP)), "Swamp"));
        addSpellEffect(new DiscardEffect(new AmountThisWay(StepKind.RETURN), Subjects.targetPlayer()));
    }
}
