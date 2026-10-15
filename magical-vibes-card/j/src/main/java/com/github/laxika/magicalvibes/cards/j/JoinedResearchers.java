package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.cards.s.SecretRendezvous;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AnOpponentHasMoreCardsInHandThanController;
import com.github.laxika.magicalvibes.model.effect.BecomePreparedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;

/**
 * Joined Researchers // Secret Rendezvous (SOS 23).
 *
 * <p>At the beginning of each end step, if an opponent has more cards in hand than you, this
 * creature becomes prepared. Its prepared spell is {@link SecretRendezvous}.
 */
@CardRegistration(set = "SOS", collectorNumber = "23")
public class JoinedResearchers extends Card {

    public JoinedResearchers() {
        setBackFaceCard(new SecretRendezvous());

        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new AnOpponentHasMoreCardsInHandThanController(), new BecomePreparedEffect()));
    }

    @Override
    public String getBackFaceClassName() {
        return "SecretRendezvous";
    }
}
