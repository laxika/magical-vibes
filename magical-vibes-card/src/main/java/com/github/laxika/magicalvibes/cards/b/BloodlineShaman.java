package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardOfChosenCreatureTypeToHandElseGraveyardEffect;

import java.util.List;

@CardRegistration(set = "ONS", collectorNumber = "249")
public class BloodlineShaman extends Card {

    public BloodlineShaman() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new RevealTopCardOfChosenCreatureTypeToHandElseGraveyardEffect()),
                "{T}: Choose a creature type. Reveal the top card of your library. If that card is a creature card of the chosen type, put it into your hand. Otherwise, put it into your graveyard."
        ));
    }
}
