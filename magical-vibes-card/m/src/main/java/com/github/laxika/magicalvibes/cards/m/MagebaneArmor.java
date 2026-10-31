package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PreventAllNoncombatDamageToAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;

@CardRegistration(set = "M10", collectorNumber = "214")
public class MagebaneArmor extends Card {

    public MagebaneArmor() {
        addStaticEffect(new StaticBoostEffect(2, 4, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new RemoveKeywordEffect(Keyword.FLYING, GrantScope.EQUIPPED_CREATURE));
        addStaticEffect(new PreventAllNoncombatDamageToAttachedCreatureEffect());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
