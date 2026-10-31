package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.OpponentsAttackedThisTurn;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnSnapshotEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "TMC", collectorNumber = "24")
public class FastForward extends Card {

    public FastForward() {
        addStaticEffect(new ReduceOwnCastCostEffect(new OpponentsAttackedThisTurn()));
        addSpellEffect(new GoadCreaturesUntilNextTurnSnapshotEffect(
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
