package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

import java.util.List;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "9ED", collectorNumber = "60")
@CardRegistration(set = "8ED", collectorNumber = "60")
public class Archivist extends Card {

    public Archivist() {
        // {T}: Draw a card.
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new DrawCardEffect()),
                "{T}: Draw a card."
        ));
    }
}
