package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.amount.VotesReceived;
import com.github.laxika.magicalvibes.model.effect.PutCountersEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.TapPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "LTC", collectorNumber = "25")
@CardRegistration(set = "LTC", collectorNumber = "108")
@CardRegistration(set = "LTC", collectorNumber = "427")
public class TrapTheTrespassers extends Card {

    public TrapTheTrespassers() {
        addSpellEffect(VoteEffect.forPermanents(Subjects.controlledBy(Subjects.eachOpponent(),
                new PermanentIsCreaturePredicate())).castSecretly());
        addSpellEffect(new PutCountersEffect(Subjects.votedFor(), CounterType.STUN,
                VotesReceived.byPermanent()));
        addSpellEffect(new TapPermanentsEffect(Subjects.votedFor()));
    }
}
