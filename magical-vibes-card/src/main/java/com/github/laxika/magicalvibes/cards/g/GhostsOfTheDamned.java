package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

import java.util.List;

@CardRegistration(set = "LEG", collectorNumber = "98")
public class GhostsOfTheDamned extends Card {

    public GhostsOfTheDamned() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BoostTargetCreatureEffect(-1, 0)),
                "{T}: Target creature gets -1/-0 until end of turn."
        ));
    }
}
