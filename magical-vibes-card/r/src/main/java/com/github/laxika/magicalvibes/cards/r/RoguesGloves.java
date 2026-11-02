package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M15", collectorNumber = "227")
@CardRegistration(set = "M19", collectorNumber = "243")
@CardRegistration(set = "SLZ", collectorNumber = "111")
@CardRegistration(set = "SLZ", collectorNumber = "232")
@CardRegistration(set = "SLZ", collectorNumber = "353")
public class RoguesGloves extends Card {

    public RoguesGloves() {
        // Whenever equipped creature deals combat damage to a player, you may draw a card.
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(new DrawCardEffect(1), "Draw a card?"));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
