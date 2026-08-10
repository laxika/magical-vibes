package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "MRD", collectorNumber = "178")
public class GoblinReplica extends Card {

    public GoblinReplica() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{R}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{3}{R}, Sacrifice this creature: Destroy target artifact.",
                TargetFilters.artifact()
        ));
    }
}
