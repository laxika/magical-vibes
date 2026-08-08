package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EmptyHandDrawExtraCardAndLoseLifeEffect;

/**
 * Blood Scrivener — "If you would draw a card while you have no cards in hand, instead you draw
 * two cards and you lose 1 life."
 */
@CardRegistration(set = "DGM", collectorNumber = "22")
public class BloodScrivener extends Card {

    public BloodScrivener() {
        addEffect(EffectSlot.STATIC, new EmptyHandDrawExtraCardAndLoseLifeEffect());
    }
}
