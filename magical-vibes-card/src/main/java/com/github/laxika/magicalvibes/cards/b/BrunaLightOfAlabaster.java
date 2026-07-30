package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AttachAurasToSourceEffect;

/**
 * Bruna, Light of Alabaster — "Whenever Bruna attacks or blocks, you may attach to it any number of
 * Auras on the battlefield and you may put onto the battlefield attached to it any number of Aura
 * cards that could enchant it from your graveyard and/or hand."
 */
@CardRegistration(set = "AVR", collectorNumber = "208")
public class BrunaLightOfAlabaster extends Card {

    public BrunaLightOfAlabaster() {
        addEffect(EffectSlot.ON_ATTACK, new AttachAurasToSourceEffect());
        addEffect(EffectSlot.ON_BLOCK, new AttachAurasToSourceEffect());
    }
}
