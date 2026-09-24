package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;

import java.util.Set;

@CardRegistration(set = "DST", collectorNumber = "148")
@CardRegistration(set = "MMA", collectorNumber = "216")
@CardRegistration(set = "MPS", collectorNumber = "29")
@CardRegistration(set = "2XM", collectorNumber = "297")
@CardRegistration(set = "HA7", collectorNumber = "20")
@CardRegistration(set = "SPG", collectorNumber = "62")
@CardRegistration(set = "MAR", collectorNumber = "100")
public class SwordOfFireAndIce extends Card {

    public SwordOfFireAndIce() {
        addEffect(EffectSlot.STATIC, new StaticBoostEffect(2, 2, GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(
                Set.of(CardColor.RED, CardColor.BLUE), GrantScope.EQUIPPED_CREATURE));
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER,
                new DealDamageToAnyTargetEffect(2));
        addEffect(EffectSlot.ON_EQUIPPED_CREATURE_DEALS_COMBAT_DAMAGE_TO_PLAYER,
                new DrawCardEffect(1));
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
