package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayDrawCardThenGainLifeEffect;

import java.util.List;

@CardRegistration(set = "BLC", collectorNumber = "90")
@CardRegistration(set = "BLC", collectorNumber = "254")
public class KwainItinerantMeddler extends Card {

    public KwainItinerantMeddler() {
        addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new EachPlayerMayDrawCardThenGainLifeEffect()),
                "{T}: Each player may draw a card, then each player who drew a card this way gains 1 life."));
    }
}
