package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DefendingPlayerMayDrawCardEffect;

@CardRegistration(set = "MIR", collectorNumber = "266")
public class HarborGuardian extends Card {

    public HarborGuardian() {
        // Reach is loaded from Scryfall metadata.
        // Whenever this creature attacks, defending player may draw a card.
        addEffect(EffectSlot.ON_ATTACK, new DefendingPlayerMayDrawCardEffect());
    }
}
