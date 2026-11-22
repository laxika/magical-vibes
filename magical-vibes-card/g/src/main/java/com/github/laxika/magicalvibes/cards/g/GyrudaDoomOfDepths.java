package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaValueParity;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.CardManaValueParityPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "221")
@CardRegistration(set = "MUL", collectorNumber = "42")
@CardRegistration(set = "MUL", collectorNumber = "107")
@CardRegistration(set = "MUL", collectorNumber = "172")
public class GyrudaDoomOfDepths extends Card {

    public GyrudaDoomOfDepths() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MillEffect(4, Subjects.eachPlayer()),
                ChooseCardEffect.announced(
                        Subjects.cardsThisWay(StepKind.MILL, new CardAllOfPredicate(List.of(
                                new CardTypePredicate(CardType.CREATURE),
                                new CardManaValueParityPredicate(ManaValueParity.EVEN)))),
                        "a creature card with an even mana value to put onto the battlefield"),
                new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE))
                        .underControlOf(Subjects.you()));
    }
}
