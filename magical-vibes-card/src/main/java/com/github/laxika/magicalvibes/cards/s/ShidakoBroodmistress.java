package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

import java.util.List;

/**
 * Flipped face of {@link com.github.laxika.magicalvibes.cards.o.OrochiEggwatcher}.
 */
public class ShidakoBroodmistress extends Card {

    public ShidakoBroodmistress() {
        // "{G}, Sacrifice a creature: Target creature gets +3/+3 until end of turn." - the sacrifice is
        // a cost, so it may eat this creature itself.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{G}",
                List.of(
                        new SacrificeCreatureCost(),
                        new BoostTargetCreatureEffect(3, 3)
                ),
                "{G}, Sacrifice a creature: Target creature gets +3/+3 until end of turn."
        ));
    }
}
