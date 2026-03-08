package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerByHandSizeEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifePerCardsInHandEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;

import java.util.Set;

@CardRegistration(set = "NPH", collectorNumber = "161")
public class SwordOfWarAndPeace extends Card {

    public SwordOfWarAndPeace() {
        // Static: equipped creature gets +2/+2
        addEffect(EffectSlot.STATIC, new BoostAttachedCreatureEffect(2, 2));

        // Static: equipped creature has protection from red and from white
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.RED, CardColor.WHITE)));

        // Triggered: whenever equipped creature deals combat damage to a player,
        // Sword of War and Peace deals damage to that player equal to the number of cards in their hand
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DealDamageToTargetPlayerByHandSizeEffect());

        // Triggered: ...and you gain 1 life for each card in your hand
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new GainLifePerCardsInHandEffect());

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
