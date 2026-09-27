package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "CMM", collectorNumber = "408")
@CardRegistration(set = "CMM", collectorNumber = "615")
@CardRegistration(set = "C15", collectorNumber = "53")
public class Scytheclaw extends Card {

    public Scytheclaw() {
        // Living weapon — create a 0/0 black Phyrexian Germ token and attach
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LivingWeaponEffect());

        // Equipped creature gets +1/+1
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));

        // Whenever equipped creature deals combat damage to a player, that player loses half
        // their life, rounded up
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new LoseLifeEffect(new HalvedRoundedUp(new TargetPlayerLifeTotal()),
                        LoseLifeRecipient.TARGET_PLAYER));

        // Equip {3}
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
