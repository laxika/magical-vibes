package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentNamedPredicate;

import java.util.List;

@CardRegistration(set = "HBG", collectorNumber = "53")
public class GenasiRabbleRouser extends Card {

    public GenasiRabbleRouser() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{1}{R}",
                List.of(new BoostAllOwnCreaturesEffect(1, 0,
                        new PermanentNamedPredicate("Genasi Rabble-Rouser"))),
                "{1}{R}: Creatures you control named Genasi Rabble-Rouser get +1/+0 until end of turn."
        ));
    }
}
