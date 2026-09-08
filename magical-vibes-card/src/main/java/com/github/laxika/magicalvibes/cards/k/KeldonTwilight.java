package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.NoCreaturesAttackedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledContinuouslySinceBeginningOfTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "112")
public class KeldonTwilight extends Card {

    public KeldonTwilight() {
        // At the beginning of each player's end step, if no creatures attacked this turn, that
        // player sacrifices a creature they controlled since the beginning of the turn.
        addEffect(EffectSlot.END_STEP_TRIGGERED, new ConditionalEffect(
                new NoCreaturesAttackedThisTurn(),
                new SacrificePermanentsEffect(
                        1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledContinuouslySinceBeginningOfTurnPredicate())),
                        SacrificeRecipient.ACTIVE_PLAYER)));
    }
}
