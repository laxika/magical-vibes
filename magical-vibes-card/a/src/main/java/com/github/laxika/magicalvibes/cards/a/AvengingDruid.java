package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCardsOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "EXO", collectorNumber = "105")
public class AvengingDruid extends Card {

    public AvengingDruid() {
        var landCard = new CardTypePredicate(CardType.LAND);
        addTrigger(Triggers.dealsDamageToOpponent(),
                new MayEffect(SequenceEffect.of(
                                new RevealCardsFromTopUntilEffect(landCard),
                                new PutCardsOntoBattlefieldEffect(Subjects.revealedThisWay(landCard)),
                                new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.restRevealedThisWay(landCard))),
                        "Reveal cards until you reveal a land card?"));
    }
}
