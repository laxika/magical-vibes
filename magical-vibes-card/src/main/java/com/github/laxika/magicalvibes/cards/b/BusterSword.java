package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.MayCastAnySpellFromHandWithoutPayingManaCostEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "FIN", collectorNumber = "255")
public class BusterSword extends Card {

    public BusterSword() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(3, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DrawCardEffect(1));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayCastAnySpellFromHandWithoutPayingManaCostEffect(new EventValue()));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
