package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.OpponentsAttackedThisTurn;
import com.github.laxika.magicalvibes.model.effect.GoadCreaturesUntilNextTurnEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "TMC", collectorNumber = "24")
public class FastForward extends Card {

    public FastForward() {
        addEffect(EffectSlot.STATIC, new ReduceOwnCastCostEffect(new OpponentsAttackedThisTurn()));
        addEffect(EffectSlot.SPELL, new GoadCreaturesUntilNextTurnEffect(
                new PermanentNotPredicate(new PermanentControlledBySourceControllerPredicate())));
    }
}
