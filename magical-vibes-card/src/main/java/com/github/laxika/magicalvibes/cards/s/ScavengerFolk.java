package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "7ED", collectorNumber = "267")
public class ScavengerFolk extends Card {

    public ScavengerFolk() {
        // {G}, {T}, Sacrifice this creature: Destroy target artifact.
        addActivatedAbility(new ActivatedAbility(
                true,
                "{G}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect()),
                "{G}, {T}, Sacrifice Scavenger Folk: Destroy target artifact.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsArtifactPredicate(),
                        "Target must be an artifact"
                )
        ));
    }
}
