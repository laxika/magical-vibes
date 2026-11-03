package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesThisTurn;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardToSourceAndMayPlayWhileConditionEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSC", collectorNumber = "702")
public class PickUpThePace extends Card {

    public PickUpThePace() {
        addTrigger(Triggers.creatureYouControlAttacks()
                        .withFilter(new PermanentEnteredBattlefieldThisTurnPredicate()),
                new ExileTopCardToSourceAndMayPlayWhileConditionEffect(new AttackedWithCreaturesThisTurn(1)));
    }
}
