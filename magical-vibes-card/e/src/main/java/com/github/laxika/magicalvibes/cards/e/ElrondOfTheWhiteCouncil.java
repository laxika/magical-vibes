package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.StaticAbility;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.VotesFor;
import com.github.laxika.magicalvibes.model.condition.VoteResult;
import com.github.laxika.magicalvibes.model.effect.CantAttackCardOwnerEffect;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnEachControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;
import java.util.List;

@CardRegistration(set = "LTC", collectorNumber = "51")
@CardRegistration(set = "LTC", collectorNumber = "134")
@CardRegistration(set = "LTC", collectorNumber = "454")
public class ElrondOfTheWhiteCouncil extends Card {

    public static final String FELLOWSHIP = "Fellowship";
    public static final String AID = "Aid";

    public ElrondOfTheWhiteCouncil() {
        // Secret council — each player secretly votes for fellowship or aid. For each fellowship vote, the voter
        // chooses a creature they control (starting with the active player). You gain control of each creature chosen
        // this way, and they gain "This creature can't attack its owner." Then for each aid vote, put a +1/+1 counter
        // on each creature you control.
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                VoteEffect.secretly(FELLOWSHIP, AID),
                ChoosePermanentsEffect.forEachVote(FELLOWSHIP, Subjects.allCreatures(),
                        ChoosePermanentsEffect.VoterRelation.CONTROLS, true, "a creature"),
                GainControlOfTargetEffect.of(Subjects.thisWay(StepKind.CHOOSE), Duration.PERMANENT),
                new GrantEffectToTargetEffect(new StaticAbility(List.of(new CantAttackCardOwnerEffect(false))),
                        Duration.PERMANENT, false, Subjects.thisWay(StepKind.CHOOSE)),
                new ConditionalEffect(VoteResult.anyVotes(AID), new PutCounterOnEachControlledPermanentEffect(
                        CounterType.PLUS_ONE_PLUS_ONE, new VotesFor(AID), new PermanentIsCreaturePredicate()))));
    }
}
