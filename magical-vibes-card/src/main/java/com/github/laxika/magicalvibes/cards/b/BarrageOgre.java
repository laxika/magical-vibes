package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;

import java.util.List;

@CardRegistration(set = "SOM", collectorNumber = "83")
public class BarrageOgre extends Card {

    public BarrageOgre() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new SacrificeArtifactCost(), new DealDamageToAnyTargetEffect(2)),
                "{T}, Sacrifice an artifact: Barrage Ogre deals 2 damage to any target."
        ));
    }
}
