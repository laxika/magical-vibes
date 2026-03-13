package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesLifeEffect;

import java.util.List;

@CardRegistration(set = "M10", collectorNumber = "83")
public class AcolyteOfXathrid extends Card {

    public AcolyteOfXathrid() {
        // {1}{B}, {T}: Target player loses 1 life.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{1}{B}",
                List.of(new TargetPlayerLosesLifeEffect(1)),
                "{1}{B}, {T}: Target player loses 1 life."
        ));
    }
}
