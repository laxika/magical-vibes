package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerPoisoned;
import com.github.laxika.magicalvibes.model.effect.CantAttackUnlessEffect;

@CardRegistration(set = "NPH", collectorNumber = "30")
public class ChainedThroatseeker extends Card {

    public ChainedThroatseeker() {
        // Infect is auto-loaded from Scryfall.
        // Can't attack unless defending player is poisoned.
        addEffect(EffectSlot.STATIC, new CantAttackUnlessEffect(
                new DefendingPlayerPoisoned(),
                "defending player is poisoned"
        ));
    }
}
