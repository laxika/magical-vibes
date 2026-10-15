package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegenerateEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "DST", collectorNumber = "10")
public class PteronGhost extends Card {

    public PteronGhost() {
        // Sacrifice this creature: Regenerate target artifact.
        addActivatedAbility(new ActivatedAbility(
                false,
                null,
                List.of(new SacrificeSelfCost(), new RegenerateEffect(true)),
                "Sacrifice Pteron Ghost: Regenerate target artifact.",
                TargetFilters.artifact()
        ));
    }
}
