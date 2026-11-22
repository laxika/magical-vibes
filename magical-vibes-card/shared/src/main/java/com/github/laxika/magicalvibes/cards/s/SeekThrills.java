package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.CardHasAdventurePredicate;

public class SeekThrills extends Card {

    public SeekThrills() {
        addSpellEffect(SequenceEffect.of(
                new MillEffect(7, Subjects.you()),
                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.MILL, new CardHasAdventurePredicate()))));
    }
}
