package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.CopyImprintedCardAndMayCastCopyEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "ONE", collectorNumber = "125")
public class CapriciousHellraiser extends Card {

    public CapriciousHellraiser() {
        addStaticEffect(new ConditionalEffect(
                new GraveyardCardThreshold(9, null), new ReduceOwnCastCostEffect(new Fixed(3))));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                SequenceEffect.of(
                        ExileCardAtRandomEffect.fromGraveyard(Subjects.you(), null).cards(3),
                        ChooseCardEffect.announced(Subjects.cardsThisWay(StepKind.EXILE, eligibleCopyFilter()),
                                "a noncreature, nonland card exiled this way to copy"),
                        CopyImprintedCardAndMayCastCopyEffect.of(Subjects.cardsThisWay(StepKind.CHOOSE))));
    }

    private static CardPredicate eligibleCopyFilter() {
        return new CardAllOfPredicate(List.of(
                new CardNotPredicate(new CardTypePredicate(CardType.CREATURE)),
                new CardNotPredicate(new CardTypePredicate(CardType.LAND))));
    }
}
