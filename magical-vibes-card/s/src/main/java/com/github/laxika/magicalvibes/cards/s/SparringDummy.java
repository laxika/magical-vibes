package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

@CardRegistration(set = "TLA", collectorNumber = "197")
public class SparringDummy extends Card {

    public SparringDummy() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new MillEffect(1, Subjects.you()),
                        ChooseCardEffect.upTo(
                                Subjects.cardsThisWay(StepKind.MILL, new CardTypePredicate(CardType.LAND)), 1,
                                "a land card milled this way"),
                        new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE)),
                        new ConditionalEffect(
                                HappenedThisWay.toACard(StepKind.MILL, new CardSubtypePredicate(CardSubtype.LESSON)),
                                new GainLifeEffect(2))),
                "{T}: Mill a card. You may put a land card milled this way into your hand. You gain 2 life if a Lesson card is milled this way."
        ));
    }
}
