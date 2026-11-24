package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.RegisterDelayedStepTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TurnSourceFaceUpEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsFaceDownPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "LGN", collectorNumber = "111")
public class SkirkAlarmist extends Card {

    public SkirkAlarmist() {
        addActivatedAbility(new ActivatedAbility(
                true,
                null,
                List.of(
                        TurnSourceFaceUpEffect.ofTarget(),
                        RegisterDelayedStepTriggerEffect.aboutAtNext(Triggers.endStep(ActivePlayer.ANY),
                                Subjects.target(),
                                SacrificeSelfEffect.ofTriggeringPermanent())
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
