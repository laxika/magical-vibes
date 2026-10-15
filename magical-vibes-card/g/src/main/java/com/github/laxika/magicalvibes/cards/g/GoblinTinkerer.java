package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetArtifactDealManaValueDamageToSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "180")
@CardRegistration(set = "ATH", collectorNumber = "40")
public class GoblinTinkerer extends Card {

    public GoblinTinkerer() {
        addActivatedAbility(new ActivatedAbility(
                true,
                "{R}",
                List.of(new DestroyTargetArtifactDealManaValueDamageToSourceEffect()),
                "{R}, {T}: Destroy target artifact. That artifact deals damage equal to its mana value to this creature.",
                new PermanentPredicateTargetFilter(
                        new PermanentIsArtifactPredicate(),
                        "Target must be an artifact"
                )
        ));
    }
}
