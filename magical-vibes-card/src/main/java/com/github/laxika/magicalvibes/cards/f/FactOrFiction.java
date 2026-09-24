package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;

@CardRegistration(set = "INV", collectorNumber = "57")
@CardRegistration(set = "DD2", collectorNumber = "26")
@CardRegistration(set = "VMA", collectorNumber = "67")
@CardRegistration(set = "JVC", collectorNumber = "26")
@CardRegistration(set = "V13", collectorNumber = "9")
@CardRegistration(set = "EMA", collectorNumber = "48")
@CardRegistration(set = "MH1", collectorNumber = "50")
@CardRegistration(set = "DMR", collectorNumber = "48")
@CardRegistration(set = "GN3", collectorNumber = "27")
@CardRegistration(set = "CMD", collectorNumber = "45")
@CardRegistration(set = "CMM", collectorNumber = "91")
@CardRegistration(set = "CMM", collectorNumber = "631")
public class FactOrFiction extends Card {

    public FactOrFiction() {
        addEffect(EffectSlot.SPELL, new RevealTopCardsAndSeparateEffect(5));
    }
}
