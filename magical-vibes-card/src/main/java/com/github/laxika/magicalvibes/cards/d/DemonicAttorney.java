package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AnteRecipient;
import com.github.laxika.magicalvibes.model.effect.AnteTopCardEffect;

@CardRegistration(set = "SUM", collectorNumber = "103")
public class DemonicAttorney extends Card {

    public DemonicAttorney() {
        // The deck-construction instruction about ante is not an in-game effect.
        addEffect(EffectSlot.SPELL, new AnteTopCardEffect(AnteRecipient.EACH_PLAYER));
    }
}
