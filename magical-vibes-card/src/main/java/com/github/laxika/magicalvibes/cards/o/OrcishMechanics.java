package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

import java.util.List;

@CardRegistration(set = "ATQ", collectorNumber = "27")
@CardRegistration(set = "ATQ", collectorNumber = "92")
@CardRegistration(set = "ME1", collectorNumber = "106")
public class OrcishMechanics extends Card {

    public OrcishMechanics() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificePermanentCost(new PermanentIsArtifactPredicate(), "an artifact", false),
                        new DealDamageToAnyTargetEffect(2)
                ),
                "{T}, Sacrifice an artifact: This creature deals 2 damage to any target."
        ));
    }
}
