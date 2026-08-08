package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.effect.RevealUntilLandsMillTargetPlayerEffect;

@CardRegistration(set = "DGM", collectorNumber = "85")
public class MirkoVoskMindDrinker extends Card {

    public MirkoVoskMindDrinker() {
        // Flying comes from the Scryfall keyword data.
        // Whenever Mirko Vosk deals combat damage to a player, that player reveals cards from the
        // top of their library until they reveal four land cards, then puts those cards into their
        // graveyard. TARGET_PLAYER is bound to the damaged player on this slot, so no target(...).
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER,
                new RevealUntilLandsMillTargetPlayerEffect(4, MillRecipient.TARGET_PLAYER));
    }
}
