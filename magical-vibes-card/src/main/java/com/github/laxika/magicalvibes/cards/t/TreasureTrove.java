package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "110")
@CardRegistration(set = "8ED", collectorNumber = "110")
public class TreasureTrove extends Card {

    public TreasureTrove() {
        // {2}{U}{U}: Draw a card.
        addActivatedAbility(new ActivatedAbility(false, "{2}{U}{U}",
                List.of(new DrawCardEffect()),
                "{2}{U}{U}: Draw a card."));
    }
}
