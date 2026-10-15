package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SacrificeTargetPermanentAtEndStepEffect;
import com.github.laxika.magicalvibes.model.effect.TurnTargetCreatureFaceUpEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "111")
public class SkirkAlarmist extends Card {

    public SkirkAlarmist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        new TurnTargetCreatureFaceUpEffect(),
                        new SacrificeTargetPermanentAtEndStepEffect()
                ),
                "{T}: Turn target face-down creature you control face up. At the beginning of the next end step, sacrifice it.",
                new PermanentPredicateTargetFilter(
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentIsFaceDownPredicate(),
                                new PermanentControlledBySourceControllerPredicate()
                        )),
                        "Target must be a face-down creature you control"
                )
        ));
    }
}
