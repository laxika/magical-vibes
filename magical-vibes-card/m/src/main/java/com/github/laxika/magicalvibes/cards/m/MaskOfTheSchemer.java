package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "NCC", collectorNumber = "28")
@CardRegistration(set = "NCC", collectorNumber = "129")
public class MaskOfTheSchemer extends Card {

    public MaskOfTheSchemer() {
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                DrawDiscardAndConniveEffect.ofTriggeringPermanent(new EventValue()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
