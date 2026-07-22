package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealUntilBasicLandToHandRestToGraveyardEffect;

import java.util.List;

@CardRegistration(set = "INR", collectorNumber = "202")
public class HermitDruid extends Card {

    public HermitDruid() {
        // {G}, {T}: Reveal cards from the top of your library until you reveal a basic land card.
        // Put that card into your hand and all other cards revealed this way into your graveyard.
        addActivatedAbility(new ActivatedAbility(
                true, "{G}",
                List.of(new RevealUntilBasicLandToHandRestToGraveyardEffect()),
                "{G}, {T}: Reveal cards from the top of your library until you reveal a basic land card. "
                        + "Put that card into your hand and all other cards revealed this way into your graveyard."
        ));
    }
}
