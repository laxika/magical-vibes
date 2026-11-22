package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "BRO", collectorNumber = "196")
public class WastefulHarvest extends Card {

    public WastefulHarvest() {
        addSpellEffect(SequenceEffect.of(
                new MillEffect(5, Subjects.you()),
                ChooseCardEffect.upTo(Subjects.cardsThisWay(StepKind.MILL, new CardIsPermanentPredicate()), 1,
                        "a permanent card milled this way"),
                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE))));
    }
}
