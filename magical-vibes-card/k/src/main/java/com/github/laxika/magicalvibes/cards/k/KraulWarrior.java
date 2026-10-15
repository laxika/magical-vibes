package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;

import java.util.List;

@CardRegistration(set = "DGM", collectorNumber = "42")
public class KraulWarrior extends Card {

    public KraulWarrior() {
        // {5}{G}: This creature gets +3/+3 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{5}{G}",
                List.of(new BoostSelfEffect(3, 3)),
                "{5}{G}: This creature gets +3/+3 until end of turn."));
    }
}
