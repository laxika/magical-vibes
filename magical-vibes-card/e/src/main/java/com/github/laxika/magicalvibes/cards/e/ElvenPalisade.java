package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "EXO", collectorNumber = "109")
public class ElvenPalisade extends Card {

    public ElvenPalisade() {
        // Sacrifice a Forest: Target attacking creature gets -3/-0 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{0}",
                List.of(
                        new SacrificePermanentCost(
                                new PermanentHasSubtypePredicate(CardSubtype.FOREST),
                                "Sacrifice a Forest",
                                false
                        ),
                        new BoostTargetCreatureEffect(-3, 0)
                ),
                "Sacrifice a Forest: Target attacking creature gets -3/-0 until end of turn.",
                TargetFilters.attackingCreature()
        ));
    }
}
