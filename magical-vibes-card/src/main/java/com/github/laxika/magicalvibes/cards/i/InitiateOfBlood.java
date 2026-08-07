package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.g.GokaTheUnjust;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ResolveEffectOnTargetDeathThisTurnEffect;
import com.github.laxika.magicalvibes.model.effect.TransformToBackFaceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentDealtDamageThisTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "CHK", collectorNumber = "173")
public class InitiateOfBlood extends Card {

    public InitiateOfBlood() {
        setBackFaceCard(new GokaTheUnjust());

        // "{T}: This creature deals 1 damage to target creature that was dealt damage this turn. When
        // that creature dies this turn, flip this creature." - the second sentence is a delayed trigger
        // watching the same target, so the flip happens whatever ends up killing it.
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new DealDamageToTargetCreatureEffect(1),
                        new ResolveEffectOnTargetDeathThisTurnEffect(new TransformToBackFaceEffect())
                ),
                "{T}: Initiate of Blood deals 1 damage to target creature that was dealt damage this turn. "
                        + "When that creature dies this turn, flip Initiate of Blood.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentDealtDamageThisTurnPredicate()
                        )),
                        "Target must be a creature that was dealt damage this turn"
                )
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "GokaTheUnjust";
    }
}
