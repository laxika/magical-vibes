package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.condition.HappenedThisWay;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledByDefendingPlayerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "MIR", collectorNumber = "71")
public class KukemssaPirates extends Card {

    public KukemssaPirates() {
        // Whenever this creature attacks and isn't blocked, you may gain control of target artifact
        // defending player controls. If you do, this creature assigns no combat damage this turn.
        addTrigger(Triggers.attacksUnblocked(),
                new MayEffect(
                        SequenceEffect.of(
                                GainControlOfTargetEffect.withTargetPredicate(Duration.PERMANENT,
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentIsArtifactPredicate(),
                                                new PermanentControlledByDefendingPlayerPredicate()))),
                                new ConditionalEffect(new HappenedThisWay(StepKind.GAIN_CONTROL),
                                        new AssignNoCombatDamageEffect())),
                        "You may gain control of target artifact defending player controls. "
                                + "If you do, this creature assigns no combat damage this turn."));
    }
}
