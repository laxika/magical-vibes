package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

public class ZoologicalStudy extends Card {

    public ZoologicalStudy() {
        addSpellEffect(SequenceEffect.of(
                new MillEffect(5, Subjects.you()),
                ChooseCardEffect.announced(Subjects.cardsThisWay(StepKind.MILL, new CardTypePredicate(CardType.CREATURE)),
                        "a creature card milled this way"),
                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE))));
    }
}
