package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.DefendingPlayerPoisoned;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessEffect;

@CardRegistration(set = "NPH", collectorNumber = "30")
public class ChainedThroatseeker extends Card {

    public ChainedThroatseeker() {
        // Infect is auto-loaded from Scryfall.
        // Can't attack unless defending player is poisoned.
        addStaticEffect(CantAttackOrBlockUnlessEffect.attack(
                new DefendingPlayerPoisoned(),
                "defending player is poisoned"
        ));
    }
}
