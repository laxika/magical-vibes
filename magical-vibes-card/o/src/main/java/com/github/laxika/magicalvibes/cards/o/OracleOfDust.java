package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRecipient;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutOpponentOwnedExiledCardIntoGraveyardCost;

import java.util.List;

@CardRegistration(set = "BFZ", collectorNumber = "63")
public class OracleOfDust extends Card {

    public OracleOfDust() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new PutOpponentOwnedExiledCardIntoGraveyardCost(),
                        new DrawCardEffect(),
                        new DiscardEffect(1, DiscardRecipient.CONTROLLER)),
                "{2}, Put a card an opponent owns from exile into that player's graveyard: Draw a card, then discard a card."
        ));
    }
}
