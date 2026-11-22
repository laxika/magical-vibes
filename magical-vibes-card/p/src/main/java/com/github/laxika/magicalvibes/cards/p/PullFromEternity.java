package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "TSP", collectorNumber = "35")
public class PullFromEternity extends Card {

    public PullFromEternity() {
        addSpellEffect(new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.targetExiledCard(null)));
    }
}
