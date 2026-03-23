package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.FirstTargetDealsPowerDamageToSecondTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutPlusOnePlusOneCounterOnTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.ControlledPermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "XLN", collectorNumber = "285")
public class HuatliDinosaurKnight extends Card {

    public HuatliDinosaurKnight() {
        // +2: Put two +1/+1 counters on up to one target Dinosaur you control.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new PutPlusOnePlusOneCounterOnTargetCreatureEffect(2)),
                "+2: Put two +1/+1 counters on up to one target Dinosaur you control.",
                new ControlledPermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)
                        )),
                        "Target must be a Dinosaur creature you control"
                ),
                2, null, null,
                List.of(), 0, 1
        ));

        // −3: Target Dinosaur you control deals damage equal to its power to target creature you don't control.
        addActivatedAbility(new ActivatedAbility(
                false, null,
                List.of(new FirstTargetDealsPowerDamageToSecondTargetEffect()),
                "\u22123: Target Dinosaur you control deals damage equal to its power to target creature you don't control.",
                null, -3, null, null,
                List.of(
                        new ControlledPermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR)
                                )),
                                "First target must be a Dinosaur creature you control"
                        ),
                        new PermanentPredicateTargetFilter(
                                new PermanentAllOfPredicate(List.of(
                                        new PermanentIsCreaturePredicate(),
                                        new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())
                                )),
                                "Second target must be a creature you don't control"
                        )
                ),
                2, 2
        ));

        // −7: Dinosaurs you control get +4/+4 until end of turn.
        addActivatedAbility(new ActivatedAbility(
                -7,
                List.of(new BoostAllOwnCreaturesEffect(4, 4, new PermanentHasSubtypePredicate(CardSubtype.DINOSAUR))),
                "\u22127: Dinosaurs you control get +4/+4 until end of turn."
        ));
    }
}
