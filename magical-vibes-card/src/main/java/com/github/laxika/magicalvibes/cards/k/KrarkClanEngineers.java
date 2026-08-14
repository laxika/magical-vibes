package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeMultiplePermanentsCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "5DN", collectorNumber = "70")
public class KrarkClanEngineers extends Card {

    public KrarkClanEngineers() {
        addActivatedAbility(new ActivatedAbility(
                false,
                "{R}",
                List.of(
                        new SacrificeMultiplePermanentsCost(2, new PermanentIsArtifactPredicate()),
                        new DestroyTargetPermanentEffect()
                ),
                "{R}, Sacrifice two artifacts: Destroy target artifact.",
                TargetFilters.artifact()
        ));
    }
}
