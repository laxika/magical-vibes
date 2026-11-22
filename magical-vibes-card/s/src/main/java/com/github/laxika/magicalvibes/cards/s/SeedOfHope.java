package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "MOM", collectorNumber = "204")
public class SeedOfHope extends Card {

    public SeedOfHope() {
        addSpellEffect(SequenceEffect.of(
                new MillEffect(2, Subjects.you()),
                ChooseCardEffect.upTo(Subjects.cardsThisWay(StepKind.MILL, new CardIsPermanentPredicate()), 1,
                        "a permanent card milled this way"),
                new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE))));
        addSpellEffect(new GainLifeEffect(2));
    }
}
