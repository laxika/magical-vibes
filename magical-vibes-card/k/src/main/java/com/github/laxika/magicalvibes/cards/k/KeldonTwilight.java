package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.NoCreaturesAttackedThisTurn;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledContinuouslySinceBeginningOfTurnPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.ActivePlayer;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "PLS", collectorNumber = "112")
public class KeldonTwilight extends Card {

    public KeldonTwilight() {
        // At the beginning of each player's end step, if no creatures attacked this turn, that
        // player sacrifices a creature they controlled since the beginning of the turn.
        addTrigger(Triggers.endStep(ActivePlayer.ANY), new ConditionalEffect(
                new NoCreaturesAttackedThisTurn(),
                new SacrificePermanentsEffect(
                        1,
                        new PermanentAllOfPredicate(List.of(
                                new PermanentIsCreaturePredicate(),
                                new PermanentControlledContinuouslySinceBeginningOfTurnPredicate())),
                        SacrificeRecipient.ACTIVE_PLAYER)));
    }
}
