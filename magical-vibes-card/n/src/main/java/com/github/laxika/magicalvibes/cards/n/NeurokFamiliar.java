package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MRD", collectorNumber = "43")
public class NeurokFamiliar extends Card {

    public NeurokFamiliar() {
        var artifactCard = new CardTypePredicate(CardType.ARTIFACT);
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new RevealTopCardOfLibraryEffect(Subjects.you()),
                new PutCardsIntoHandEffect(Subjects.revealedThisWay(artifactCard)),
                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.revealedThisWay(new CardNotPredicate(artifactCard))));
    }
}
