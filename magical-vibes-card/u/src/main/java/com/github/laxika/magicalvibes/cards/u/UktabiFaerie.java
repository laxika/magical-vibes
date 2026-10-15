package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "247")
public class UktabiFaerie extends Card {

    public UktabiFaerie() {
        // Flying is loaded from Scryfall.
        // {3}{G}, Sacrifice this creature: Destroy target artifact.
        addActivatedAbility(new ActivatedAbility(
                false,
                "{3}{G}",
                List.of(new SacrificeSelfCost(), new DestroyTargetPermanentEffect(false)),
                "{3}{G}, Sacrifice Uktabi Faerie: Destroy target artifact.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsArtifactPredicate(),
                        "Target must be an artifact"
                )
        ));
    }
}
