package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ISD", collectorNumber = "224")
public class GhoulcallersBell extends Card {

    public GhoulcallersBell() {
        addActivatedAbility(new ActivatedAbility(
                true, null,
                List.of(new MillEffect(1, Subjects.you()), new MillEffect(1, Subjects.eachOpponent())),
                "{T}: Each player mills a card."
        ));
    }
}
