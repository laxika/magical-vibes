package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetPermanentFromOpponentGraveyardToHandThenDefendingPlayerChoosesPermanentFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;

@CardRegistration(set = "40K", collectorNumber = "135")
public class NeyamShaiMurad extends Card {

    public NeyamShaiMurad() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new ReturnTargetPermanentFromOpponentGraveyardToHandThenDefendingPlayerChoosesPermanentFromGraveyardEffect(
                        new CardIsPermanentPredicate()));
    }
}
