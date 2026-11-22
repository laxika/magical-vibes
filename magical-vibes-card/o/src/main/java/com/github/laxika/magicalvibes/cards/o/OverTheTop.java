package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.CountScope;
import com.github.laxika.magicalvibes.model.amount.PermanentCount;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "BRO", collectorNumber = "146")
public class OverTheTop extends Card {

    public OverTheTop() {
        CardPredicate permanentCard = new CardIsPermanentPredicate();
        addSpellEffect(SequenceEffect.of(
                RevealTopCardOfLibraryEffect.topCards(Subjects.eachPlayer(), new PermanentCount(
                        new PermanentNotPredicate(new PermanentIsLandPredicate()), CountScope.CONTROLLER)),
                new PutCardsOntoBattlefieldEffect(Subjects.cardsThisWay(StepKind.REVEAL, permanentCard)),
                new PutExiledCardsIntoOwnersGraveyardEffect(
                        Subjects.cardsThisWay(StepKind.REVEAL, new CardNotPredicate(permanentCard)))));
    }
}
