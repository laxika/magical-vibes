package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookDestination;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardMayPlayFreeEffect;

@CardRegistration(set = "AVR", collectorNumber = "173")
public class DescendantsPath extends Card {

    public DescendantsPath() {
        // At the beginning of your upkeep, reveal the top card of your library. If it's a creature
        // card that shares a creature type with a creature you control, you may cast it without
        // paying its mana cost. If you don't cast it, put it on the bottom of your library.
        addEffect(EffectSlot.UPKEEP_TRIGGERED,
                new RevealTopCardMayPlayFreeEffect(LookDestination.BOTTOM_OF_LIBRARY, true));
    }
}
