package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroySelfAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "8ED", collectorNumber = "182")
public class CinderWall extends Card {

    public CinderWall() {
        // Defender is a keyword auto-loaded from Scryfall.
        // When this creature blocks, destroy it at end of combat.
        addEffect(EffectSlot.ON_BLOCK, new DestroySelfAtEndOfCombatEffect());
    }
}
