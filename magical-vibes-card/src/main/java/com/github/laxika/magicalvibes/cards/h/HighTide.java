package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.LandsOfSubtypeAddExtraManaUntilEndOfTurnEffect;

@CardRegistration(set = "ME1", collectorNumber = "35")
@CardRegistration(set = "VMA", collectorNumber = "73")
public class HighTide extends Card {

    public HighTide() {
        addEffect(EffectSlot.SPELL,
                new LandsOfSubtypeAddExtraManaUntilEndOfTurnEffect(CardSubtype.ISLAND, ManaColor.BLUE));
    }
}
