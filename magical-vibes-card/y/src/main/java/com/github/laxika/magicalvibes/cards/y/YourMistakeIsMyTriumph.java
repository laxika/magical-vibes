package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "DSC", collectorNumber = "364")
public class YourMistakeIsMyTriumph extends Card {

    public YourMistakeIsMyTriumph() {
        addSpellEffect(new MillEffect(3, Subjects.eachPlayer()));
        addSpellEffect(ChooseCardEffect.announced(
                Subjects.cardsThisWay(StepKind.MILL, new CardIsPermanentPredicate()),
                "a milled permanent card to put onto the battlefield"));
        addSpellEffect(new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE))
                .underControlOf(Subjects.you()));
    }
}
