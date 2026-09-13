package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AdditionalTriggeredAbilityEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "NEO", collectorNumber = "224")
public class IsshinTwoHeavensAsOne extends Card {

    public IsshinTwoHeavensAsOne() {
        addEffect(EffectSlot.STATIC, AdditionalTriggeredAbilityEffect.forAttackTriggers(
                new PermanentTruePredicate(), null));
    }
}
