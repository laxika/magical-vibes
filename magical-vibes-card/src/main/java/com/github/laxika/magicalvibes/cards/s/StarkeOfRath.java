package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPermanentControllerGainsControlOfSourceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TMP", collectorNumber = "205")
@CardRegistration(set = "TPR", collectorNumber = "162")
public class StarkeOfRath extends Card {

    public StarkeOfRath() {
        // {T}: Destroy target artifact or creature. That permanent's controller gains control of
        // Starke of Rath. The control change is listed first so the target's controller is still
        // readable; both effects resolve in the same pass.
        addActivatedAbility(new ActivatedAbility(
                true, "",
                List.of(
                        new TargetPermanentControllerGainsControlOfSourceEffect(ControlDuration.PERMANENT),
                        new DestroyTargetPermanentEffect(false)
                ),
                "{T}: Destroy target artifact or creature. That permanent's controller gains control of Starke of Rath.",
                new PermanentPredicateTargetFilter(
                        new PermanentAnyOfPredicate(List.of(
                                new PermanentIsArtifactPredicate(),
                                new PermanentIsCreaturePredicate()
                        )),
                        "Target must be an artifact or creature"
                )
        ));
    }
}
