package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.AttackedWithCreaturesThisTurn;
import com.github.laxika.magicalvibes.model.effect.ExileTopCardToSourceAndMayPlayWhileConditionEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentEnteredBattlefieldThisTurnPredicate;

@CardRegistration(set = "MSC", collectorNumber = "702")
public class PickUpThePace extends Card {

    public PickUpThePace() {
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS, new TriggeringPermanentConditionalEffect(
                new PermanentEnteredBattlefieldThisTurnPredicate(),
                new ExileTopCardToSourceAndMayPlayWhileConditionEffect(new AttackedWithCreaturesThisTurn(1))));
    }
}
