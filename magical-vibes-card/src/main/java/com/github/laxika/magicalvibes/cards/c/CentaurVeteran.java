package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;

import java.util.List;

@CardRegistration(set = "TOR", collectorNumber = "123")
public class CentaurVeteran extends Card {

    public CentaurVeteran() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(new DiscardCardTypeCost(null, null), new RegenerateEffect()),
                "{G}, Discard a card: Regenerate Centaur Veteran."
        ));
    }
}
