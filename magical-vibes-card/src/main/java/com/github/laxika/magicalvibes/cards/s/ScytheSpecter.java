package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ScytheSpecterEffect;

@CardRegistration(set = "CMD", collectorNumber = "97")
public class ScytheSpecter extends Card {

    public ScytheSpecter() {
        // Flying is loaded from Scryfall metadata.
        // Whenever this creature deals combat damage to a player, each opponent discards a card.
        // Players who discarded a card with the greatest mana value this way lose that much life.
        addEffect(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER, new ScytheSpecterEffect());
    }
}
