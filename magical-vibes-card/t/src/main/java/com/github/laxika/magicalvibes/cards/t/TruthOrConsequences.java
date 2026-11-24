package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.amount.VotesFor;
import com.github.laxika.magicalvibes.model.condition.VoteResult;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.effect.ChoosePlayerAtRandomEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToPlayersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;

@CardRegistration(set = "WHO", collectorNumber = "163")
@CardRegistration(set = "WHO", collectorNumber = "768")
public class TruthOrConsequences extends Card {

    public static final String TRUTH = "Truth";
    public static final String CONSEQUENCES = "Consequences";

    public TruthOrConsequences() {
        addSpellEffect(VoteEffect.secretly(TRUTH, CONSEQUENCES));
        addSpellEffect(new ConditionalEffect(VoteResult.anyVotes(TRUTH), new DrawCardEffect(new VotesFor(TRUTH))));
        addSpellEffect(new ChoosePlayerAtRandomEffect(Subjects.eachOpponent()));
        addSpellEffect(new ConditionalEffect(VoteResult.anyVotes(CONSEQUENCES),
                new DealDamageToPlayersEffect(new Scaled(new VotesFor(CONSEQUENCES), 3),
                        Subjects.playersThisWay(StepKind.CHOOSE))));
    }
}
