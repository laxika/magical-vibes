package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTopUntilNonlandOfDamagedPlayerMayCounterOrCastEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MSH", collectorNumber = "89")
public class BlackWidowSuperSpy extends Card {

    public BlackWidowSuperSpy() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new ExileTopUntilNonlandOfDamagedPlayerMayCounterOrCastEffect());
    }
}
