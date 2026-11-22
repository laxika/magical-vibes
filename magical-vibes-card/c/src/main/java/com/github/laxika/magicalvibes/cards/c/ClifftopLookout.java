package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "168")
@CardRegistration(set = "FDC", collectorNumber = "195")
public class ClifftopLookout extends Card {

    public ClifftopLookout() {
        var landCard = new CardTypePredicate(CardType.LAND);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new RevealCardsFromTopUntilEffect(landCard),
                new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(landCard)).enteringTapped(),
                PutCardsIntoLibraryEffect.onBottomInRandomOrder(Subjects.restRevealedThisWay(landCard)));
    }
}
