package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "DST", collectorNumber = "140")
@CardRegistration(set = "V09", collectorNumber = "12")
@CardRegistration(set = "VMA", collectorNumber = "281")
@CardRegistration(set = "SLD", collectorNumber = "1112")
@CardRegistration(set = "SLD", collectorNumber = "1663")
@CardRegistration(set = "CMD", collectorNumber = "260")
@CardRegistration(set = "SLZ", collectorNumber = "113")
@CardRegistration(set = "SLZ", collectorNumber = "234")
@CardRegistration(set = "SLZ", collectorNumber = "355")
@CardRegistration(set = "C14", collectorNumber = "268")
@CardRegistration(set = "C15", collectorNumber = "267")
public class Skullclamp extends Card {

    public Skullclamp() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, -1, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DIES, new DrawCardEffect(2));
        addActivatedAbility(new EquipActivatedAbility("{1}"));
    }
}
