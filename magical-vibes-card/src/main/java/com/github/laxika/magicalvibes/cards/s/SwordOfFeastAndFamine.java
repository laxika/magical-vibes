package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.ProtectionFromColorsEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAllControlledPermanentsEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsLandPredicate;

import java.util.Set;

@CardRegistration(set = "MBS", collectorNumber = "138")
public class SwordOfFeastAndFamine extends Card {

    public SwordOfFeastAndFamine() {
        // Static: equipped creature gets +2/+2
        addEffect(EffectSlot.STATIC, new BoostAttachedCreatureEffect(2, 2));

        // Static: equipped creature has protection from black and from green
        addEffect(EffectSlot.STATIC, new ProtectionFromColorsEffect(Set.of(CardColor.BLACK, CardColor.GREEN)));

        // Triggered: whenever equipped creature deals combat damage to a player,
        // that player discards a card
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new TargetPlayerDiscardsEffect(1));

        // Triggered: ...and you untap all lands you control
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new UntapAllControlledPermanentsEffect(new PermanentIsLandPredicate()));

        // Equip {2}
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
