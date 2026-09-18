package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.amount.HalvedRoundedUp;
import com.github.laxika.magicalvibes.model.amount.TargetPlayerLifeTotal;
import com.github.laxika.magicalvibes.model.effect.LivingWeaponEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeRecipient;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "C15", collectorNumber = "53")
public class Scytheclaw extends Card {

    public Scytheclaw() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new LivingWeaponEffect());
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(1, 1, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new LoseLifeEffect(new HalvedRoundedUp(new TargetPlayerLifeTotal()), LoseLifeRecipient.TARGET_PLAYER));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
