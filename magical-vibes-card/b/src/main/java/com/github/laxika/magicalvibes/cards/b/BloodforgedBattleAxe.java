package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

@CardRegistration(set = "2X2", collectorNumber = "299")
@CardRegistration(set = "C17", collectorNumber = "50")
@CardRegistration(set = "PIP", collectorNumber = "226")
@CardRegistration(set = "PIP", collectorNumber = "481")
@CardRegistration(set = "PIP", collectorNumber = "754")
@CardRegistration(set = "PIP", collectorNumber = "1009")
@CardRegistration(set = "MOC", collectorNumber = "349")
public class BloodforgedBattleAxe extends Card {

    public BloodforgedBattleAxe() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 0, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new CreateTokenCopyOfSourceEffect());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
