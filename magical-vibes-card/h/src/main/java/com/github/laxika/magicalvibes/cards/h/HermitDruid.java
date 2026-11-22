package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.PutCardsIntoHandEffect;
import com.github.laxika.magicalvibes.model.effect.PutExiledCardsIntoOwnersGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.RevealCardsFromTopUntilEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.CardPredicateUtils;
import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "202")
@CardRegistration(set = "STH", collectorNumber = "108")
@CardRegistration(set = "TPR", collectorNumber = "175")
@CardRegistration(set = "VMA", collectorNumber = "216")
public class HermitDruid extends Card {

    public HermitDruid() {
        // {G}, {T}: Reveal cards from the top of your library until you reveal a basic land card.
        // Put that card into your hand and all other cards revealed this way into your graveyard.
        addActivatedAbility(new ActivatedAbility(
                true, "{G}",
                List.of(new RevealCardsFromTopUntilEffect(CardPredicateUtils.basicLand()),
                        new PutCardsIntoHandEffect(Subjects.revealedThisWay(CardPredicateUtils.basicLand())),
                        new PutExiledCardsIntoOwnersGraveyardEffect(Subjects.revealedThisWay())),
                "{G}, {T}: Reveal cards from the top of your library until you reveal a basic land card. "
                        + "Put that card into your hand and all other cards revealed this way into your graveyard."
        ));
    }
}
