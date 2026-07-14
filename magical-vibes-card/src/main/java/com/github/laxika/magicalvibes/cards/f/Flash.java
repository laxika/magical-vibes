package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PutCreatureFromHandThenSacrificeUnlessPayReducedEffect;

@CardRegistration(set = "6ED", collectorNumber = "67")
public class Flash extends Card {

    public Flash() {
        // You may put a creature card from your hand onto the battlefield. If you do, sacrifice it
        // unless you pay its mana cost reduced by {2}.
        addEffect(EffectSlot.SPELL, new PutCreatureFromHandThenSacrificeUnlessPayReducedEffect(2));
    }
}
