package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

import java.util.List;

@CardRegistration(set = "6ED", collectorNumber = "114")
public class BlightedShaman extends Card {

    public BlightedShaman() {
        // {T}, Sacrifice a Swamp: Target creature gets +1/+1 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.SWAMP),
                                "Sacrifice a Swamp"
                        ),
                        new BoostTargetCreatureEffect(1, 1)
                ),
                "{T}, Sacrifice a Swamp: Target creature gets +1/+1 until end of turn."
        ));

        // {T}, Sacrifice a creature: Target creature gets +2/+2 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeCreatureCost(),
                        new BoostTargetCreatureEffect(2, 2)
                ),
                "{T}, Sacrifice a creature: Target creature gets +2/+2 until end of turn."
        ));
    }
}
