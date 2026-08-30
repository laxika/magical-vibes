package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedSelfReturnTransformedEffect;
import com.github.laxika.magicalvibes.model.effect.SourceFightsTargetCreatureEffect;

import java.util.List;

@CardRegistration(set = "RIX", collectorNumber = "179")
public class GoldenGuardian extends Card {

    public GoldenGuardian() {
        setBackFaceCard(new GoldForgeGarrison());

        addActivatedAbility(new ActivatedAbility(
                false,
                "{2}",
                List.of(
                        new SourceFightsTargetCreatureEffect(),
                        new RegisterDelayedSelfReturnTransformedEffect()
                ),
                "{2}: This creature fights another target creature you control. When this creature dies this turn, return it to the battlefield transformed under your control.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate())
                        )),
                        "Target must be another creature you control"
                )
        ));
    }

    @Override
    public String getBackFaceClassName() {
        return "GoldForgeGarrison";
    }
}
