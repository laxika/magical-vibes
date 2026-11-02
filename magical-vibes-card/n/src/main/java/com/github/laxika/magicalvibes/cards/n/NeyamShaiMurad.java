package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentFromOpponentGraveyardToHandThenDefendingPlayerChoosesPermanentFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "40K", collectorNumber = "135")
public class NeyamShaiMurad extends Card {

    public NeyamShaiMurad() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(
                        new ReturnTargetPermanentFromOpponentGraveyardToHandThenDefendingPlayerChoosesPermanentFromGraveyardEffect(
                                new CardIsPermanentPredicate()),
                        "Have that player return the targeted card to their hand?"));
    }
}
