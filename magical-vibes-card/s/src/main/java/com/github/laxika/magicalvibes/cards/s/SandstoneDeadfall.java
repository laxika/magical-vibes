package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "ODY", collectorNumber = "307")
public class SandstoneDeadfall extends Card {

    public SandstoneDeadfall() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new SacrificeMultiplePermanentsCost(2, new PermanentIsLandPredicate()),
                        new SacrificeSelfCost(),
                        new DestroyTargetPermanentEffect()
                ),
                "{T}, Sacrifice two lands and Sandstone Deadfall: Destroy target attacking creature.",
                TargetFilters.attackingCreature()
        ));
    }
}
