package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardAndConniveEffect;

@CardRegistration(set = "NCC", collectorNumber = "28")
@CardRegistration(set = "NCC", collectorNumber = "129")
public class MaskOfTheSchemer extends Card {

    public MaskOfTheSchemer() {
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new DrawDiscardAndConniveEffect(new EventValue()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
