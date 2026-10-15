package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;

import java.util.List;

@CardRegistration(set = "IKO", collectorNumber = "199")
public class Parcelbeast extends Card {

    public Parcelbeast() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}",
                List.of(new LookAtTopCardMayPutMatchingOntoBattlefieldElseToHandEffect(
                        new CardTypePredicate(CardType.LAND), false)),
                "{1}, {T}: Look at the top card of your library. If it's a land card, you may put it onto the battlefield. If you don't put the card onto the battlefield, put it into your hand."
        ));
    }
}
