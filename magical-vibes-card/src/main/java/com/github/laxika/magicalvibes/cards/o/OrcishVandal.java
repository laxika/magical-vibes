package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;

import java.util.List;

@CardRegistration(set = "DOM", collectorNumber = "137")
public class OrcishVandal extends Card {

    public OrcishVandal() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeArtifactCost(), new DealDamageToAnyTargetEffect(2)),
                "{T}, Sacrifice an artifact: Orcish Vandal deals 2 damage to any target."
        ));
    }
}
