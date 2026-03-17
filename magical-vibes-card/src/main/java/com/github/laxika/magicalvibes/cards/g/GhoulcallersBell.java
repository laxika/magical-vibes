package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachOpponentMillsEffect;
import com.github.laxika.magicalvibes.model.effect.MillControllerEffect;

import java.util.List;

@CardRegistration(set = "ISD", collectorNumber = "224")
public class GhoulcallersBell extends Card {

    public GhoulcallersBell() {
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new MillControllerEffect(1), new EachOpponentMillsEffect(1)),
                "{T}: Each player mills a card."
        ));
    }
}
