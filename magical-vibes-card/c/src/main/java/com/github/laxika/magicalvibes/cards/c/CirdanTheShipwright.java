package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.VotesReceived;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.EachPlayerMayPutCardFromHandToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.VoteEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "LTC", collectorNumber = "50")
@CardRegistration(set = "LTC", collectorNumber = "133")
@CardRegistration(set = "LTC", collectorNumber = "453")
public class CirdanTheShipwright extends Card {

    public CirdanTheShipwright() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), secretCouncil());
        addTrigger(Triggers.attacks(), secretCouncil());
    }

    /**
     * "Each player secretly votes for a player, then those votes are revealed. Each player draws a card for each vote
     * they received. Each player who received no votes may put a permanent card from their hand onto the battlefield."
     */
    private static CardEffect secretCouncil() {
        return SequenceEffect.of(
                VoteEffect.forPlayers(Subjects.eachPlayer()).castSecretly(),
                DrawCardEffect.eachEvaluatedFor(VotesReceived.byPlayer(), Subjects.eachPlayer()),
                EachPlayerMayPutCardFromHandToBattlefieldEffect.by(Subjects.playersWithNoVotes(),
                        new CardIsPermanentPredicate(), "permanent"));
    }
}
