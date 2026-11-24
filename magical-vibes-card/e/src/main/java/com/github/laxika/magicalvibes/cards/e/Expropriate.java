package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.VotesFor;
import com.github.laxika.magicalvibes.model.condition.VoteResult;
import com.github.laxika.magicalvibes.model.effect.ChoosePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerExtraTurnEffect;
import com.github.laxika.magicalvibes.model.effect.Duration;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;

@CardRegistration(set = "SPG", collectorNumber = "66")
@CardRegistration(set = "SLD", collectorNumber = "2396")
public class Expropriate extends Card {

    public static final String TIME = "Time";
    public static final String MONEY = "Money";

    public Expropriate() {
        // Council's dilemma — Starting with you, each player votes for time or money. For each time vote, take an
        // extra turn after this one. For each money vote, choose a permanent owned by the voter and gain control of it.
        addSpellEffect(VoteEffect.startingWithYou(TIME, MONEY));
        addSpellEffect(new ConditionalEffect(VoteResult.anyVotes(TIME),
                new ControllerExtraTurnEffect(new VotesFor(TIME))));
        addSpellEffect(ChoosePermanentsEffect.forEachVote(MONEY, Subjects.allPermanents(),
                ChoosePermanentsEffect.VoterRelation.OWNS, false, "a permanent"));
        addSpellEffect(GainControlOfTargetEffect.of(Subjects.thisWay(StepKind.CHOOSE), Duration.PERMANENT));
        addSpellEffect(new ExileSpellEffect());
    }
}
