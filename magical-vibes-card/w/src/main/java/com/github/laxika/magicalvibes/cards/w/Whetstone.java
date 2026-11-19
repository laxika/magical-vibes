package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;

import java.util.List;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "USG", collectorNumber = "316")
public class Whetstone extends Card {

    public Whetstone() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}",
                List.of(
                        new MillEffect(2, Subjects.you()),
                        new MillEffect(2, Subjects.eachOpponent())
                ),
                "{3}: Each player mills two cards."
        ));
    }
}
