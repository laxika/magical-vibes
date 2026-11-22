package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;


@CardRegistration(set = "FRF", collectorNumber = "139")
public class SuddenReclamation extends Card {

    public SuddenReclamation() {
        addSpellEffect(new MillEffect(4, Subjects.you()));
        addSpellEffect(ChooseCardEffect.oneOfEach(Subjects.cardsInYourGraveyard(null),
                new ChooseCardEffect.Each(new CardTypePredicate(CardType.CREATURE), "a creature card"),
                new ChooseCardEffect.Each(new CardTypePredicate(CardType.LAND), "a land card")));
        addSpellEffect(new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE)));
    }
}
