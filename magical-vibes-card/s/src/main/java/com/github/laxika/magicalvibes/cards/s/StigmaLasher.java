package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerCantGainLifeRestOfGameEffect;

@CardRegistration(set = "EVE", collectorNumber = "62")
public class StigmaLasher extends Card {

    public StigmaLasher() {
        // Wither is auto-loaded from Scryfall keywords.
        // Whenever this creature deals damage to a player, that player can't gain life for the rest of
        // the game. The damaged player is carried as the trigger's (non-targeting) target.
        addEffect(EffectSlot.ON_DAMAGE_TO_PLAYER, new TargetPlayerCantGainLifeRestOfGameEffect());
    }
}
