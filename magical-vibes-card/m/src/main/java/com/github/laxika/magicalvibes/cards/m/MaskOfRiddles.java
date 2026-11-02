package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ARB", collectorNumber = "25")
@CardRegistration(set = "NCC", collectorNumber = "347")
public class MaskOfRiddles extends Card {

    public MaskOfRiddles() {
        // Static: equipped creature has fear
        addStaticEffect(new GrantKeywordEffect(Keyword.FEAR, GrantScope.EQUIPPED_CREATURE));

        // Triggered: whenever equipped creature deals combat damage to a player, you may draw a card
        addTrigger(Triggers.dealsCombatDamageToPlayer(),
                new MayEffect(new DrawCardEffect(1), "Draw a card?"));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
