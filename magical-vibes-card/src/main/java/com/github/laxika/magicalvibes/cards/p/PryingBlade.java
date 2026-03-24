package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "XLN", collectorNumber = "244")
public class PryingBlade extends Card {

    public PryingBlade() {
        // Equipped creature gets +1/+0.
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 0, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature deals combat damage to a player, create a Treasure token.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, CreateTokenEffect.ofTreasureToken(1));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
