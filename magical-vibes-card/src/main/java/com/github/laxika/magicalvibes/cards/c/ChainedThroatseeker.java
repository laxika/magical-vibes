package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessDefenderPoisonedEffect;

@CardRegistration(set = "NPH", collectorNumber = "30")
public class ChainedThroatseeker extends Card {

    public ChainedThroatseeker() {
        // Infect is auto-loaded from Scryfall.
        // Can't attack unless defending player is poisoned.
        addEffect(EffectSlot.STATIC, new CantAttackUnlessDefenderPoisonedEffect());
    }
}
