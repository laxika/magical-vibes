package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ROE", collectorNumber = "86")
@CardRegistration(set = "PC2", collectorNumber = "25")
@CardRegistration(set = "PCA", collectorNumber = "25")
public class SeeBeyond extends Card {

    public SeeBeyond() {
        addSpellEffect(new DrawCardEffect(2));
        addSpellEffect(ChooseCardEffect.oneOf(Subjects.cardsInYourHand(null),
                "a card from your hand to shuffle into your library"));
        addSpellEffect(PutCardsIntoLibraryEffect.shuffledIn(Subjects.cardsThisWay(StepKind.CHOOSE)));
    }
}
