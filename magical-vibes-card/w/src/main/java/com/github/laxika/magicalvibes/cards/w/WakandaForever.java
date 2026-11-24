package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ChooseCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "MSC", collectorNumber = "71")
@CardRegistration(set = "MSC", collectorNumber = "388")
public class WakandaForever extends Card {

    public WakandaForever() {
        var permanentCard = new CardIsPermanentPredicate();
        addSpellEffect(RevealTopCardOfLibraryEffect.topCards(Subjects.you(), new Fixed(6)));
        addSpellEffect(ChooseCardEffect.upTo(Subjects.revealedThisWay(permanentCard), 1,
                "a permanent card to put onto the battlefield with an indestructible counter"));
        addSpellEffect(new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.CHOOSE))
                .withCounters(CounterType.INDESTRUCTIBLE, 1));
        addSpellEffect(ChooseCardEffect.upTo(Subjects.revealedThisWay(permanentCard), 1,
                "a permanent card to put into your hand"));
        addSpellEffect(new PutCardsIntoHandEffect(Subjects.cardsThisWay(StepKind.CHOOSE)));
        addSpellEffect(new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.revealedThisWay()));
    }
}
