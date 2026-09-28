package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachCreatureDamagedPlayerControlsEffect;

@CardRegistration(set = "ISD", collectorNumber = "129")
@CardRegistration(set = "INR", collectorNumber = "479")
@CardRegistration(set = "UMA", collectorNumber = "124")
@CardRegistration(set = "SIS", collectorNumber = "37")
@CardRegistration(set = "CMM", collectorNumber = "207")
@CardRegistration(set = "CMM", collectorNumber = "530")
@CardRegistration(set = "CMM", collectorNumber = "697")
public class BalefireDragon extends Card {

    public BalefireDragon() {
        // Flying is auto-loaded from Scryfall
        // Whenever Balefire Dragon deals combat damage to a player,
        // it deals that much damage to each creature that player controls.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new DealDamageToEachCreatureDamagedPlayerControlsEffect());
    }
}
