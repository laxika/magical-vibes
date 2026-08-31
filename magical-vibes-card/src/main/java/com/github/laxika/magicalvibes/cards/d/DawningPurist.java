package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyPermanentDamagedPlayerControlsEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

@CardRegistration(set = "ONS", collectorNumber = "22")
public class DawningPurist extends Card {

    public DawningPurist() {
        addMorph("{1}{W}");

        // Whenever this creature deals combat damage to a player, you may destroy target enchantment
        // that player controls.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new MayEffect(
                        new DestroyPermanentDamagedPlayerControlsEffect(
                                new PermanentIsEnchantmentPredicate(), 0),
                        "You may destroy target enchantment that player controls."));
    }
}
