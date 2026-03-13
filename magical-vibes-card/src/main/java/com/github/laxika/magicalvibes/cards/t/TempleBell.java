package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.EachPlayerDrawsCardEffect;

import java.util.List;

@CardRegistration(set = "M11", collectorNumber = "217")
public class TempleBell extends Card {

    public TempleBell() {
        // {T}: Each player draws a card.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new EachPlayerDrawsCardEffect(1)),
                "{T}: Each player draws a card."
        ));
    }
}
