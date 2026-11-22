package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ZEN", collectorNumber = "126")
@CardRegistration(set = "MM3", collectorNumber = "96")
@CardRegistration(set = "2XM", collectorNumber = "127")
@CardRegistration(set = "MB2", collectorNumber = "58")
public class GoblinGuide extends Card {

    public GoblinGuide() {
        addTrigger(Triggers.attacks(),
                new RevealTopCardOfLibraryEffect(Subjects.defendingPlayer()),
                new PutCardsIntoHandEffect(Subjects.revealedThisWay(new CardTypePredicate(CardType.LAND))));
    }
}
