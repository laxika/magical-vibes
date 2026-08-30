package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardSameNameInGraveyardOrDrawEffect;
import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "250")
public class CandlesOfLeng extends Card {

    public CandlesOfLeng() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{4}",
                List.of(new RevealTopCardSameNameInGraveyardOrDrawEffect()),
                "{4}, {T}: Reveal the top card of your library. If it has the same name as a card in your graveyard, "
                        + "put it into your graveyard. Otherwise, draw a card."
        ));
    }
}
