package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "BNG", collectorNumber = "106")
public class RecklessReveler extends Card {

    public RecklessReveler() {
        // {R}, Sacrifice this creature: Destroy target artifact.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{R}, Sacrifice Reckless Reveler: Destroy target artifact.",
                TargetFilters.artifact()
        ));
    }
}
