package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

import java.util.List;

@CardRegistration(set = "DFT", collectorNumber = "84")
public class EngineRat extends Card {

    public EngineRat() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{B}",
                List.of(new LoseLifeEffect(2, Subjects.eachOpponent())),
                "{5}{B}: Each opponent loses 2 life."
        ));
    }
}
