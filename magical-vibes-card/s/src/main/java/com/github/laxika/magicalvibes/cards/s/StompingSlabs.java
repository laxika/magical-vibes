package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsBottomThenDamageIfCopyRevealedEffect;

@CardRegistration(set = "MOR", collectorNumber = "107")
public class StompingSlabs extends Card {

    public StompingSlabs() {
        // Reveal the top seven cards; bottom them in any order. If a copy of Stomping Slabs was
        // revealed, deal 7 damage to any target (target chosen on cast — no damage if none revealed).
        addEffect(EffectSlot.SPELL, new RevealTopCardsBottomThenDamageIfCopyRevealedEffect(7, 7));
    }
}
