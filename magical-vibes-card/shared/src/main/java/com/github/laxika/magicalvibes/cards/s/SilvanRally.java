package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

public class SilvanRally extends Card {

    public SilvanRally() {
        addSpellEffect(SequenceEffect.of(
                new MillEffect(4, Subjects.you()),
                ChooseCardEffect.upTo(Subjects.cardsThisWay(StepKind.MILL, new CardTypePredicate(CardType.LAND)), 2,
                        "land cards milled this way"),
                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE))));
    }
}
