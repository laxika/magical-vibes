package com.github.laxika.magicalvibes.cards.o;

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
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "5ED", collectorNumber = "258")
@CardRegistration(set = "ICE", collectorNumber = "211")
@CardRegistration(set = "ME2", collectorNumber = "143")
public class OrcishSquatters extends Card {

    public OrcishSquatters() {
        // Whenever this creature attacks and isn't blocked, you may gain control of target land
        // defending player controls for as long as you control this creature. If you do, this
        // creature assigns no combat damage this turn.
        addTrigger(Triggers.attacksUnblocked(),
                new MayEffect(
                        SequenceEffect.of(
                                GainControlOfTargetEffect.withTargetPredicate(Duration.WHILE_SOURCE_ON_BATTLEFIELD,
                                        new PermanentAllOfPredicate(List.of(
                                                new PermanentIsLandPredicate(),
                                                new PermanentControlledByDefendingPlayerPredicate()))),
                                new ConditionalEffect(new HappenedThisWay(StepKind.GAIN_CONTROL),
                                        new AssignNoCombatDamageEffect())),
                        "You may gain control of target land defending player controls for as long as "
                                + "you control this creature. If you do, this creature assigns no combat damage this turn."));
    }
}
