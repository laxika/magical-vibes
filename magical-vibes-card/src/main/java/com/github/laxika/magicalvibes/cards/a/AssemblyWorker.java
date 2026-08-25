package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "TSP", collectorNumber = "248")
public class AssemblyWorker extends Card {

    private static final PermanentAllOfPredicate ASSEMBLY_WORKER_CREATURE = new PermanentAllOfPredicate(List.of(
            new PermanentIsCreaturePredicate(),
            new PermanentHasSubtypePredicate(CardSubtype.ASSEMBLY_WORKER)
    ));

    public AssemblyWorker() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(new BoostTargetCreatureEffect(1, 1, ASSEMBLY_WORKER_CREATURE)),
                "{T}: Target Assembly-Worker creature gets +1/+1 until end of turn.",
                new PermanentPredicateTargetFilter(
                        ASSEMBLY_WORKER_CREATURE,
                        "Target must be an Assembly-Worker creature"
                )
        ));
    }
}
